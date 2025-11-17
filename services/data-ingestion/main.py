#!/usr/bin/env python3
"""
Data Ingestion Service
Real-time telemetry processing using Apache Kafka
Handles device telemetry, usage logs, and analytics data
"""
import asyncio
import json
import os
from datetime import datetime
from typing import Dict, List, Any
import logging

from kafka import KafkaConsumer, KafkaProducer
from kafka.errors import KafkaError
import psycopg2
from psycopg2.extras import execute_values
import redis
from prometheus_client import Counter, Histogram, start_http_server

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Prometheus metrics
messages_processed = Counter(
    'somolink_messages_processed_total',
    'Total number of messages processed',
    ['topic', 'status']
)
processing_duration = Histogram(
    'somolink_processing_duration_seconds',
    'Time spent processing messages',
    ['topic']
)

class DataIngestionService:
    """
    Main data ingestion service class.
    Consumes from Kafka topics, processes telemetry, and stores in TimescaleDB.
    """
    
    def __init__(self):
        # Kafka configuration
        self.kafka_bootstrap_servers = os.getenv(
            'KAFKA_BOOTSTRAP_SERVERS', 
            'localhost:9092'
        ).split(',')
        
        # Database configuration
        self.db_config = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', '5432')),
            'database': os.getenv('DB_NAME', 'somolink'),
            'user': os.getenv('DB_USER', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'postgres')
        }
        
        # Redis configuration (for caching and deduplication)
        self.redis_client = redis.Redis(
            host=os.getenv('REDIS_HOST', 'localhost'),
            port=int(os.getenv('REDIS_PORT', '6379')),
            db=int(os.getenv('REDIS_DB', '0')),
            decode_responses=True
        )
        
        # Initialize connections
        self.db_conn = None
        self.kafka_consumer = None
        self.kafka_producer = None
        
        # Topics to consume
        self.topics = [
            'device-telemetry',
            'usage-logs',
            'network-events',
            'system-alerts'
        ]
    
    def connect_database(self):
        """Establish database connection."""
        try:
            self.db_conn = psycopg2.connect(**self.db_config)
            self.db_conn.autocommit = False
            logger.info("✓ Connected to TimescaleDB")
        except Exception as e:
            logger.error(f"Failed to connect to database: {e}")
            raise
    
    def init_kafka(self):
        """Initialize Kafka consumer and producer."""
        try:
            # Consumer
            self.kafka_consumer = KafkaConsumer(
                *self.topics,
                bootstrap_servers=self.kafka_bootstrap_servers,
                group_id='somolink-ingestion-service',
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                max_poll_records=100,
                session_timeout_ms=30000,
                heartbeat_interval_ms=10000
            )
            
            # Producer (for processed data)
            self.kafka_producer = KafkaProducer(
                bootstrap_servers=self.kafka_bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                acks='all',
                retries=3
            )
            
            logger.info(f"✓ Connected to Kafka, consuming from: {self.topics}")
        except KafkaError as e:
            logger.error(f"Failed to initialize Kafka: {e}")
            raise
    
    def is_duplicate(self, message_id: str, ttl: int = 3600) -> bool:
        """
        Check if message has been processed (deduplication).
        Uses Redis with TTL.
        """
        key = f"processed:{message_id}"
        if self.redis_client.exists(key):
            return True
        self.redis_client.setex(key, ttl, "1")
        return False
    
    def process_device_telemetry(self, data: Dict[str, Any]):
        """
        Process and store device telemetry data.
        """
        with processing_duration.labels(topic='device-telemetry').time():
            try:
                cursor = self.db_conn.cursor()
                
                query = """
                INSERT INTO device_telemetry (
                    device_id, timestamp, battery_soc, battery_voltage, battery_current,
                    solar_voltage, solar_current, solar_power, cpu_temp, network_signal,
                    bandwidth_available, active_users, data_consumed_mb
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """
                
                cursor.execute(query, (
                    data.get('device_id'),
                    datetime.fromisoformat(data.get('timestamp', datetime.now().isoformat())),
                    data.get('battery_soc', 0),
                    data.get('battery_voltage', 0),
                    data.get('battery_current', 0),
                    data.get('solar_voltage', 0),
                    data.get('solar_current', 0),
                    data.get('solar_power', 0),
                    data.get('cpu_temp', 0),
                    data.get('network_signal', 0),
                    data.get('bandwidth_available', 0),
                    data.get('active_users', 0),
                    data.get('data_consumed_mb', 0)
                ))
                
                self.db_conn.commit()
                cursor.close()
                
                messages_processed.labels(
                    topic='device-telemetry',
                    status='success'
                ).inc()
                
                logger.debug(f"Stored telemetry for device {data.get('device_id')}")
                
            except Exception as e:
                self.db_conn.rollback()
                messages_processed.labels(
                    topic='device-telemetry',
                    status='error'
                ).inc()
                logger.error(f"Error processing telemetry: {e}")
                raise
    
    def process_usage_log(self, data: Dict[str, Any]):
        """
        Process and store user usage logs.
        """
        with processing_duration.labels(topic='usage-logs').time():
            try:
                cursor = self.db_conn.cursor()
                
                query = """
                INSERT INTO usage_logs (
                    device_id, user_id, timestamp, session_id, duration_minutes,
                    content_type, domain, data_consumed_mb, completion_rate,
                    num_interactions
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """
                
                cursor.execute(query, (
                    data.get('device_id'),
                    data.get('user_id'),
                    datetime.fromisoformat(data.get('timestamp', datetime.now().isoformat())),
                    data.get('session_id'),
                    data.get('duration_minutes', 0),
                    data.get('content_type'),
                    data.get('domain'),
                    data.get('data_consumed_mb', 0),
                    data.get('completion_rate', 0),
                    data.get('num_interactions', 0)
                ))
                
                self.db_conn.commit()
                cursor.close()
                
                messages_processed.labels(
                    topic='usage-logs',
                    status='success'
                ).inc()
                
            except Exception as e:
                self.db_conn.rollback()
                messages_processed.labels(
                    topic='usage-logs',
                    status='error'
                ).inc()
                logger.error(f"Error processing usage log: {e}")
                raise
    
    def process_network_event(self, data: Dict[str, Any]):
        """
        Process network connectivity events.
        """
        with processing_duration.labels(topic='network-events').time():
            try:
                # Store event
                cursor = self.db_conn.cursor()
                
                query = """
                INSERT INTO network_events (
                    device_id, timestamp, event_type, details, severity
                ) VALUES (%s, %s, %s, %s, %s)
                """
                
                cursor.execute(query, (
                    data.get('device_id'),
                    datetime.fromisoformat(data.get('timestamp', datetime.now().isoformat())),
                    data.get('event_type'),
                    json.dumps(data.get('details', {})),
                    data.get('severity', 'info')
                ))
                
                self.db_conn.commit()
                cursor.close()
                
                # If critical event, forward to alerts topic
                if data.get('severity') == 'critical':
                    self.kafka_producer.send('system-alerts', data)
                
                messages_processed.labels(
                    topic='network-events',
                    status='success'
                ).inc()
                
            except Exception as e:
                self.db_conn.rollback()
                messages_processed.labels(
                    topic='network-events',
                    status='error'
                ).inc()
                logger.error(f"Error processing network event: {e}")
                raise
    
    def route_message(self, topic: str, message: Dict[str, Any]):
        """Route message to appropriate processor."""
        # Check for duplicate
        message_id = message.get('message_id') or message.get('id') or str(message)
        if self.is_duplicate(message_id):
            logger.debug(f"Duplicate message {message_id}, skipping")
            return
        
        # Route to processor
        if topic == 'device-telemetry':
            self.process_device_telemetry(message)
        elif topic == 'usage-logs':
            self.process_usage_log(message)
        elif topic == 'network-events':
            self.process_network_event(message)
        else:
            logger.warning(f"Unknown topic: {topic}")
    
    async def run(self):
        """Main service loop."""
        logger.info("🚀 Starting Data Ingestion Service")
        
        # Initialize connections
        self.connect_database()
        self.init_kafka()
        
        # Start Prometheus metrics server
        start_http_server(8001)
        logger.info("📊 Prometheus metrics available on :8001")
        
        # Consume messages
        logger.info("📥 Consuming messages from Kafka...")
        
        try:
            for message in self.kafka_consumer:
                try:
                    self.route_message(message.topic, message.value)
                except Exception as e:
                    logger.error(f"Error processing message: {e}")
                    # Continue processing other messages
        
        except KeyboardInterrupt:
            logger.info("Shutting down gracefully...")
        
        finally:
            self.cleanup()
    
    def cleanup(self):
        """Cleanup resources."""
        logger.info("Cleaning up resources...")
        
        if self.kafka_consumer:
            self.kafka_consumer.close()
        
        if self.kafka_producer:
            self.kafka_producer.close()
        
        if self.db_conn:
            self.db_conn.close()
        
        if self.redis_client:
            self.redis_client.close()
        
        logger.info("✓ Cleanup complete")


if __name__ == "__main__":
    service = DataIngestionService()
    asyncio.run(service.run())

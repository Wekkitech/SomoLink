"""
Anomaly Detection Model
Proactive monitoring for edge device health using isolation forests and LSTM
"""
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import torch
import torch.nn as nn
from typing import Dict, List, Tuple, Optional
import pickle
import os


class LSTMAnomalyDetector(nn.Module):
    """LSTM-based anomaly detector for sequential telemetry data."""
    
    def __init__(self, input_size: int, hidden_size: int = 64, num_layers: int = 2):
        super().__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        
        self.lstm = nn.LSTM(
            input_size,
            hidden_size,
            num_layers,
            batch_first=True,
            dropout=0.2
        )
        self.fc = nn.Linear(hidden_size, input_size)
    
    def forward(self, x):
        """Forward pass - reconstruct input sequence."""
        lstm_out, _ = self.lstm(x)
        reconstruction = self.fc(lstm_out)
        return reconstruction


class AnomalyDetector:
    """
    Hybrid anomaly detection system combining:
    1. Isolation Forest for point anomalies
    2. LSTM Autoencoder for sequential anomalies
    
    Detects issues like:
    - Battery degradation
    - Network connectivity problems
    - Solar panel failures
    - Unusual usage patterns
    - Hardware malfunctions
    """
    
    def __init__(self, model_dir: Optional[str] = None):
        self.model_dir = model_dir
        self.isolation_forest = None
        self.lstm_model = None
        self.scaler = StandardScaler()
        
        # Feature definitions
        self.point_features = [
            'battery_voltage_v',
            'battery_current_a',
            'solar_voltage_v',
            'solar_current_a',
            'cpu_temp_c',
            'available_bandwidth_mbps',
            'packet_loss_rate',
            'latency_ms',
            'active_connections',
            'cpu_usage_percent',
            'memory_usage_percent',
            'disk_usage_percent'
        ]
        
        self.sequence_features = [
            'battery_soc',
            'solar_generation_w',
            'network_throughput_mbps',
            'num_users'
        ]
        
        if model_dir and os.path.exists(model_dir):
            self.load_models(model_dir)
        else:
            self._init_models()
    
    def _init_models(self):
        """Initialize detection models."""
        # Isolation Forest for point anomalies
        self.isolation_forest = IsolationForest(
            contamination=0.1,  # Expected proportion of outliers
            random_state=42,
            n_estimators=100
        )
        
        # LSTM Autoencoder for sequential anomalies
        self.lstm_model = LSTMAnomalyDetector(
            input_size=len(self.sequence_features),
            hidden_size=64,
            num_layers=2
        )
    
    def detect_point_anomaly(self, telemetry: Dict) -> Dict:
        """
        Detect point anomalies in current telemetry.
        
        Args:
            telemetry: Current device telemetry
        
        Returns:
            Detection result with anomaly score and flagged metrics
        """
        # Extract features
        features = self._extract_point_features(telemetry)
        features_scaled = self.scaler.transform(features.reshape(1, -1))
        
        # Predict anomaly
        if self.isolation_forest is None:
            # Use rule-based detection if model not trained
            return self._rule_based_point_detection(telemetry)
        
        try:
            prediction = self.isolation_forest.predict(features_scaled)[0]
            anomaly_score = self.isolation_forest.score_samples(features_scaled)[0]
            
            is_anomaly = prediction == -1
            
            # Identify which features are anomalous
            flagged_metrics = self._identify_anomalous_features(telemetry) if is_anomaly else []
            
            return {
                'is_anomaly': bool(is_anomaly),
                'anomaly_score': float(anomaly_score),
                'confidence': abs(float(anomaly_score)),
                'flagged_metrics': flagged_metrics,
                'severity': self._calculate_severity(anomaly_score, flagged_metrics),
                'recommended_action': self._get_recommended_action(flagged_metrics)
            }
        except Exception as e:
            print(f"Point anomaly detection error: {e}")
            return self._rule_based_point_detection(telemetry)
    
    def detect_sequence_anomaly(
        self, 
        telemetry_sequence: List[Dict],
        sequence_length: int = 24  # Last 24 hours
    ) -> Dict:
        """
        Detect sequential anomalies using LSTM autoencoder.
        
        Args:
            telemetry_sequence: List of telemetry dicts (time-ordered)
            sequence_length: Number of time steps to analyze
        
        Returns:
            Detection result with reconstruction error and trends
        """
        if len(telemetry_sequence) < sequence_length:
            return {
                'is_anomaly': False,
                'message': 'Insufficient data for sequence analysis',
                'reconstruction_error': 0.0
            }
        
        # Extract and scale sequence features
        sequence = self._extract_sequence_features(telemetry_sequence[-sequence_length:])
        sequence_scaled = self.scaler.transform(sequence)
        
        # Convert to tensor
        sequence_tensor = torch.FloatTensor(sequence_scaled).unsqueeze(0)
        
        if self.lstm_model is None:
            return self._rule_based_sequence_detection(telemetry_sequence)
        
        try:
            # Get reconstruction
            self.lstm_model.eval()
            with torch.no_grad():
                reconstruction = self.lstm_model(sequence_tensor)
            
            # Calculate reconstruction error
            mse = nn.MSELoss()
            reconstruction_error = mse(reconstruction, sequence_tensor).item()
            
            # Threshold for anomaly (would be learned from validation data)
            threshold = 0.5
            is_anomaly = reconstruction_error > threshold
            
            # Analyze trends
            trends = self._analyze_trends(sequence)
            
            return {
                'is_anomaly': bool(is_anomaly),
                'reconstruction_error': float(reconstruction_error),
                'threshold': threshold,
                'confidence': min(float(reconstruction_error / threshold), 1.0),
                'trends': trends,
                'severity': 'high' if reconstruction_error > threshold * 2 else 'medium' if is_anomaly else 'low'
            }
        except Exception as e:
            print(f"Sequence anomaly detection error: {e}")
            return self._rule_based_sequence_detection(telemetry_sequence)
    
    def _extract_point_features(self, telemetry: Dict) -> np.ndarray:
        """Extract point features from telemetry."""
        features = []
        for feature_name in self.point_features:
            # Convert snake_case to camelCase and extract value
            value = telemetry.get(feature_name, 0)
            features.append(float(value))
        return np.array(features)
    
    def _extract_sequence_features(self, telemetry_list: List[Dict]) -> np.ndarray:
        """Extract sequence features from telemetry list."""
        sequence = []
        for telemetry in telemetry_list:
            features = [telemetry.get(f, 0) for f in self.sequence_features]
            sequence.append(features)
        return np.array(sequence)
    
    def _identify_anomalous_features(self, telemetry: Dict) -> List[str]:
        """Identify which specific metrics are anomalous."""
        flagged = []
        
        # Battery checks
        battery_voltage = telemetry.get('battery_voltage_v', 12.0)
        if battery_voltage < 10.5 or battery_voltage > 14.5:
            flagged.append('battery_voltage')
        
        battery_soc = telemetry.get('battery_soc', 50)
        if battery_soc < 15:
            flagged.append('battery_low')
        
        # Temperature checks
        cpu_temp = telemetry.get('cpu_temp_c', 40)
        if cpu_temp > 75:
            flagged.append('cpu_overheating')
        
        # Network checks
        packet_loss = telemetry.get('packet_loss_rate', 0)
        if packet_loss > 0.05:  # >5% loss
            flagged.append('high_packet_loss')
        
        latency = telemetry.get('latency_ms', 50)
        if latency > 500:
            flagged.append('high_latency')
        
        # Resource checks
        cpu_usage = telemetry.get('cpu_usage_percent', 50)
        if cpu_usage > 90:
            flagged.append('high_cpu_usage')
        
        memory_usage = telemetry.get('memory_usage_percent', 50)
        if memory_usage > 90:
            flagged.append('high_memory_usage')
        
        return flagged
    
    def _calculate_severity(self, anomaly_score: float, flagged_metrics: List[str]) -> str:
        """Calculate severity level."""
        critical_metrics = {'battery_voltage', 'cpu_overheating', 'battery_low'}
        
        has_critical = any(m in critical_metrics for m in flagged_metrics)
        
        if has_critical or anomaly_score < -0.5:
            return 'critical'
        elif len(flagged_metrics) >= 3 or anomaly_score < -0.3:
            return 'high'
        elif len(flagged_metrics) > 0 or anomaly_score < -0.1:
            return 'medium'
        else:
            return 'low'
    
    def _get_recommended_action(self, flagged_metrics: List[str]) -> str:
        """Get recommended action based on flagged metrics."""
        if 'battery_low' in flagged_metrics:
            return 'Check battery health and charging system'
        elif 'cpu_overheating' in flagged_metrics:
            return 'Check cooling system and reduce load'
        elif 'high_packet_loss' in flagged_metrics or 'high_latency' in flagged_metrics:
            return 'Check network connection and antenna'
        elif 'high_memory_usage' in flagged_metrics or 'high_cpu_usage' in flagged_metrics:
            return 'Restart device or reduce concurrent users'
        else:
            return 'Monitor system and investigate if issue persists'
    
    def _analyze_trends(self, sequence: np.ndarray) -> Dict:
        """Analyze trends in telemetry sequence."""
        trends = {}
        
        for i, feature_name in enumerate(self.sequence_features):
            feature_data = sequence[:, i]
            
            # Simple linear trend
            slope = np.polyfit(range(len(feature_data)), feature_data, 1)[0]
            
            if abs(slope) < 0.01:
                trend = 'stable'
            elif slope > 0:
                trend = 'increasing'
            else:
                trend = 'decreasing'
            
            trends[feature_name] = {
                'trend': trend,
                'slope': float(slope),
                'current_value': float(feature_data[-1]),
                'mean_value': float(np.mean(feature_data)),
                'std_dev': float(np.std(feature_data))
            }
        
        return trends
    
    def _rule_based_point_detection(self, telemetry: Dict) -> Dict:
        """Fallback rule-based point anomaly detection."""
        flagged = self._identify_anomalous_features(telemetry)
        is_anomaly = len(flagged) > 0
        
        return {
            'is_anomaly': is_anomaly,
            'anomaly_score': -0.5 if is_anomaly else 0.5,
            'confidence': 0.7,
            'flagged_metrics': flagged,
            'severity': self._calculate_severity(-0.5 if is_anomaly else 0.5, flagged),
            'recommended_action': self._get_recommended_action(flagged),
            'method': 'rule_based'
        }
    
    def _rule_based_sequence_detection(self, telemetry_sequence: List[Dict]) -> Dict:
        """Fallback rule-based sequence anomaly detection."""
        # Check for concerning trends
        recent = telemetry_sequence[-10:]
        battery_trend = [t.get('battery_soc', 50) for t in recent]
        
        is_degrading = len(battery_trend) > 1 and all(
            battery_trend[i] > battery_trend[i+1] 
            for i in range(len(battery_trend)-1)
        )
        
        return {
            'is_anomaly': is_degrading,
            'reconstruction_error': 0.6 if is_degrading else 0.2,
            'threshold': 0.5,
            'confidence': 0.7,
            'trends': {'battery': 'degrading' if is_degrading else 'normal'},
            'severity': 'medium' if is_degrading else 'low',
            'method': 'rule_based'
        }
    
    def train(self, training_data: List[Dict], labels: Optional[List[int]] = None):
        """
        Train anomaly detection models.
        
        Args:
            training_data: Historical telemetry data
            labels: Optional labels (1 for normal, -1 for anomaly)
        """
        if not training_data:
            return
        
        # Train Isolation Forest
        X_point = np.array([self._extract_point_features(d) for d in training_data])
        self.scaler.fit(X_point)
        X_point_scaled = self.scaler.transform(X_point)
        self.isolation_forest.fit(X_point_scaled)
        
        print("Anomaly detection models trained successfully")
    
    def save_models(self, model_dir: str):
        """Save models to directory."""
        os.makedirs(model_dir, exist_ok=True)
        
        # Save Isolation Forest
        with open(os.path.join(model_dir, 'isolation_forest.pkl'), 'wb') as f:
            pickle.dump(self.isolation_forest, f)
        
        # Save scaler
        with open(os.path.join(model_dir, 'scaler.pkl'), 'wb') as f:
            pickle.dump(self.scaler, f)
        
        # Save LSTM model
        if self.lstm_model:
            torch.save(
                self.lstm_model.state_dict(),
                os.path.join(model_dir, 'lstm_model.pth')
            )
    
    def load_models(self, model_dir: str):
        """Load models from directory."""
        # Load Isolation Forest
        with open(os.path.join(model_dir, 'isolation_forest.pkl'), 'rb') as f:
            self.isolation_forest = pickle.load(f)
        
        # Load scaler
        with open(os.path.join(model_dir, 'scaler.pkl'), 'rb') as f:
            self.scaler = pickle.load(f)
        
        # Load LSTM model
        lstm_path = os.path.join(model_dir, 'lstm_model.pth')
        if os.path.exists(lstm_path):
            self.lstm_model = LSTMAnomalyDetector(
                input_size=len(self.sequence_features)
            )
            self.lstm_model.load_state_dict(torch.load(lstm_path))


__all__ = ['AnomalyDetector', 'LSTMAnomalyDetector']

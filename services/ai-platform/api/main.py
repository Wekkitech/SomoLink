"""
SomoLink AI Platform - Main FastAPI Application
Provides ML-powered predictions and analytics
"""
from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import uvicorn
from datetime import datetime, timedelta
import logging

from .models.solar import SolarForecaster
from .models.qos import QoSOptimizer
from .models.anomaly import AnomalyDetector
from .models.analytics import LearningAnalytics
from .config import Settings
from .database import get_db
from .auth import verify_api_key

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="SomoLink AI Platform",
    description="AI-powered predictions and analytics for solar-driven connectivity",
    version="1.0.0",
    docs_url="/api/docs",
    redoc_url="/api/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load settings
settings = Settings()

# Initialize ML models (lazy loading in production)
solar_forecaster = None
qos_optimizer = None
anomaly_detector = None
learning_analytics = None


# --- Request/Response Models ---

class SolarForecastRequest(BaseModel):
    device_id: str = Field(..., description="Unique device identifier")
    location: Dict[str, float] = Field(..., description="Lat/Lon coordinates")
    historical_data: Optional[List[Dict[str, Any]]] = Field(None, description="Recent solar data")
    forecast_hours: int = Field(24, ge=1, le=168, description="Hours to forecast")

class SolarForecastResponse(BaseModel):
    device_id: str
    forecast: List[Dict[str, float]]
    confidence: float
    generated_at: datetime

class QoSRecommendationRequest(BaseModel):
    device_id: str
    current_bandwidth: float  # Mbps
    connected_users: int
    time_of_day: str
    historical_usage: Optional[List[Dict[str, Any]]] = None

class QoSRecommendationResponse(BaseModel):
    device_id: str
    recommended_allocation: Dict[str, float]
    priority_classes: List[str]
    expected_satisfaction: float

class AnomalyDetectionRequest(BaseModel):
    device_id: str
    telemetry_data: Dict[str, Any]
    timestamp: datetime

class AnomalyDetectionResponse(BaseModel):
    device_id: str
    is_anomaly: bool
    anomaly_score: float
    affected_metrics: List[str]
    severity: str  # "low", "medium", "high", "critical"
    recommendations: List[str]

class LearningAnalyticsRequest(BaseModel):
    school_id: str
    start_date: datetime
    end_date: datetime
    metrics: List[str] = ["clh", "engagement", "content_access"]

class LearningAnalyticsResponse(BaseModel):
    school_id: str
    period: Dict[str, datetime]
    analytics: Dict[str, Any]
    insights: List[str]


# --- Startup/Shutdown Events ---

@app.on_event("startup")
async def startup_event():
    """Initialize models and resources on startup"""
    global solar_forecaster, qos_optimizer, anomaly_detector, learning_analytics
    
    logger.info("Loading ML models...")
    
    try:
        solar_forecaster = SolarForecaster(model_path=settings.SOLAR_MODEL_PATH)
        await solar_forecaster.load()
        logger.info("✓ Solar forecaster loaded")
    except Exception as e:
        logger.error(f"Failed to load solar forecaster: {e}")
    
    try:
        qos_optimizer = QoSOptimizer(model_path=settings.QOS_MODEL_PATH)
        await qos_optimizer.load()
        logger.info("✓ QoS optimizer loaded")
    except Exception as e:
        logger.error(f"Failed to load QoS optimizer: {e}")
    
    try:
        anomaly_detector = AnomalyDetector(model_path=settings.ANOMALY_MODEL_PATH)
        await anomaly_detector.load()
        logger.info("✓ Anomaly detector loaded")
    except Exception as e:
        logger.error(f"Failed to load anomaly detector: {e}")
    
    try:
        learning_analytics = LearningAnalytics()
        logger.info("✓ Learning analytics initialized")
    except Exception as e:
        logger.error(f"Failed to initialize learning analytics: {e}")
    
    logger.info("AI Platform startup complete")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown"""
    logger.info("Shutting down AI Platform...")
    # Close database connections, save model states, etc.


# --- Health Check Endpoints ---

@app.get("/health")
async def health_check():
    """Basic health check"""
    return {"status": "healthy", "timestamp": datetime.utcnow()}

@app.get("/health/models")
async def models_health():
    """Check status of all ML models"""
    return {
        "solar_forecaster": solar_forecaster is not None and solar_forecaster.is_loaded(),
        "qos_optimizer": qos_optimizer is not None and qos_optimizer.is_loaded(),
        "anomaly_detector": anomaly_detector is not None and anomaly_detector.is_loaded(),
        "learning_analytics": learning_analytics is not None
    }


# --- Solar Forecasting Endpoints ---

@app.post("/api/v1/solar/forecast", response_model=SolarForecastResponse)
async def predict_solar_generation(
    request: SolarForecastRequest,
    api_key: str = Depends(verify_api_key)
):
    """
    Predict solar power generation for the next N hours
    Uses LSTM model with weather data integration
    """
    if not solar_forecaster or not solar_forecaster.is_loaded():
        raise HTTPException(status_code=503, detail="Solar forecaster not available")
    
    try:
        forecast = await solar_forecaster.predict(
            device_id=request.device_id,
            location=request.location,
            historical_data=request.historical_data,
            forecast_hours=request.forecast_hours
        )
        
        return SolarForecastResponse(
            device_id=request.device_id,
            forecast=forecast["predictions"],
            confidence=forecast["confidence"],
            generated_at=datetime.utcnow()
        )
    
    except Exception as e:
        logger.error(f"Solar forecast error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# --- QoS Optimization Endpoints ---

@app.post("/api/v1/qos/recommend", response_model=QoSRecommendationResponse)
async def recommend_qos_allocation(
    request: QoSRecommendationRequest,
    api_key: str = Depends(verify_api_key)
):
    """
    Recommend optimal bandwidth allocation using contextual bandits
    Optimizes for user satisfaction and fair access
    """
    if not qos_optimizer or not qos_optimizer.is_loaded():
        raise HTTPException(status_code=503, detail="QoS optimizer not available")
    
    try:
        recommendation = await qos_optimizer.optimize(
            device_id=request.device_id,
            current_bandwidth=request.current_bandwidth,
            connected_users=request.connected_users,
            time_of_day=request.time_of_day,
            historical_usage=request.historical_usage
        )
        
        return QoSRecommendationResponse(
            device_id=request.device_id,
            recommended_allocation=recommendation["allocation"],
            priority_classes=recommendation["priority_classes"],
            expected_satisfaction=recommendation["satisfaction_score"]
        )
    
    except Exception as e:
        logger.error(f"QoS optimization error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# --- Anomaly Detection Endpoints ---

@app.post("/api/v1/anomaly/detect", response_model=AnomalyDetectionResponse)
async def detect_anomalies(
    request: AnomalyDetectionRequest,
    api_key: str = Depends(verify_api_key)
):
    """
    Detect anomalies in device telemetry
    Identifies battery issues, network problems, hardware failures
    """
    if not anomaly_detector or not anomaly_detector.is_loaded():
        raise HTTPException(status_code=503, detail="Anomaly detector not available")
    
    try:
        result = await anomaly_detector.detect(
            device_id=request.device_id,
            telemetry=request.telemetry_data,
            timestamp=request.timestamp
        )
        
        return AnomalyDetectionResponse(
            device_id=request.device_id,
            is_anomaly=result["is_anomaly"],
            anomaly_score=result["score"],
            affected_metrics=result["affected_metrics"],
            severity=result["severity"],
            recommendations=result["recommendations"]
        )
    
    except Exception as e:
        logger.error(f"Anomaly detection error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# --- Learning Analytics Endpoints ---

@app.post("/api/v1/analytics/learning", response_model=LearningAnalyticsResponse)
async def compute_learning_analytics(
    request: LearningAnalyticsRequest,
    api_key: str = Depends(verify_api_key),
    db = Depends(get_db)
):
    """
    Compute learning analytics including Connected Learning Hours (CLH)
    Provides insights for teachers and education officers
    """
    if not learning_analytics:
        raise HTTPException(status_code=503, detail="Learning analytics not available")
    
    try:
        analytics = await learning_analytics.compute(
            school_id=request.school_id,
            start_date=request.start_date,
            end_date=request.end_date,
            metrics=request.metrics,
            db=db
        )
        
        return LearningAnalyticsResponse(
            school_id=request.school_id,
            period={"start": request.start_date, "end": request.end_date},
            analytics=analytics["data"],
            insights=analytics["insights"]
        )
    
    except Exception as e:
        logger.error(f"Learning analytics error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# --- Model Management Endpoints ---

@app.post("/api/v1/models/{model_name}/retrain")
async def trigger_model_retraining(
    model_name: str,
    api_key: str = Depends(verify_api_key)
):
    """
    Trigger model retraining (async job)
    """
    valid_models = ["solar", "qos", "anomaly"]
    if model_name not in valid_models:
        raise HTTPException(status_code=404, detail=f"Model {model_name} not found")
    
    # Queue retraining job (would use Celery/Ray in production)
    logger.info(f"Retraining triggered for {model_name}")
    
    return {
        "model": model_name,
        "status": "retraining_queued",
        "job_id": f"retrain-{model_name}-{datetime.utcnow().timestamp()}"
    }

@app.get("/api/v1/models/{model_name}/metrics")
async def get_model_metrics(
    model_name: str,
    api_key: str = Depends(verify_api_key)
):
    """
    Get model performance metrics from MLflow
    """
    # Would query MLflow tracking server
    return {
        "model": model_name,
        "version": "1.2.3",
        "metrics": {
            "accuracy": 0.92,
            "mae": 0.15,
            "last_trained": "2025-11-10T08:30:00Z"
        }
    }


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )

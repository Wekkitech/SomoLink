"""
Solar Power Forecasting Model
LSTM-based time series prediction with weather data integration
"""
import torch
import torch.nn as nn
import numpy as np
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Any
import onnxruntime as ort
import requests
import logging

logger = logging.getLogger(__name__)


class SolarLSTM(nn.Module):
    """LSTM model for solar power forecasting"""
    
    def __init__(self, input_size=10, hidden_size=64, num_layers=2, output_size=1):
        super(SolarLSTM, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        
        self.lstm = nn.LSTM(
            input_size=input_size,
            hidden_size=hidden_size,
            num_layers=num_layers,
            batch_first=True,
            dropout=0.2
        )
        
        self.fc = nn.Sequential(
            nn.Linear(hidden_size, 32),
            nn.ReLU(),
            nn.Dropout(0.1),
            nn.Linear(32, output_size)
        )
    
    def forward(self, x):
        # x shape: (batch, seq_len, input_size)
        lstm_out, _ = self.lstm(x)
        # Take the last output
        last_output = lstm_out[:, -1, :]
        prediction = self.fc(last_output)
        return prediction


class SolarForecaster:
    """
    Solar power forecasting service
    Combines historical data with weather forecasts
    """
    
    def __init__(self, model_path: str, use_onnx: bool = True):
        self.model_path = model_path
        self.use_onnx = use_onnx
        self.model = None
        self.onnx_session = None
        self.scaler_params = None
        self._loaded = False
        
        # Feature indices
        self.features = [
            'panel_voltage', 'panel_current', 'panel_power',
            'irradiance', 'panel_temp', 'battery_soc',
            'hour_of_day', 'day_of_year', 'cloud_cover', 'temperature'
        ]
    
    async def load(self):
        """Load model from disk (ONNX or PyTorch)"""
        try:
            if self.use_onnx:
                # Load ONNX model for efficient inference
                self.onnx_session = ort.InferenceSession(
                    f"{self.model_path}/solar_forecast.onnx",
                    providers=['CPUExecutionProvider']
                )
                logger.info("ONNX solar model loaded")
            else:
                # Load PyTorch model
                checkpoint = torch.load(f"{self.model_path}/solar_forecast.pt")
                self.model = SolarLSTM()
                self.model.load_state_dict(checkpoint['model_state'])
                self.model.eval()
                logger.info("PyTorch solar model loaded")
            
            # Load scaler parameters
            import pickle
            with open(f"{self.model_path}/scaler.pkl", 'rb') as f:
                self.scaler_params = pickle.load(f)
            
            self._loaded = True
            
        except Exception as e:
            logger.error(f"Failed to load solar model: {e}")
            raise
    
    def is_loaded(self) -> bool:
        return self._loaded
    
    async def predict(
        self,
        device_id: str,
        location: Dict[str, float],
        historical_data: Optional[List[Dict[str, Any]]],
        forecast_hours: int = 24
    ) -> Dict[str, Any]:
        """
        Generate solar power forecast
        
        Args:
            device_id: Unique device identifier
            location: {"lat": latitude, "lon": longitude}
            historical_data: Recent telemetry data (last 48 hours recommended)
            forecast_hours: Number of hours to forecast
        
        Returns:
            Dictionary with predictions, confidence, and metadata
        """
        if not self._loaded:
            raise RuntimeError("Model not loaded")
        
        # Fetch weather forecast
        weather_forecast = await self._fetch_weather_forecast(
            location['lat'],
            location['lon'],
            forecast_hours
        )
        
        # Prepare input features
        features = self._prepare_features(
            historical_data,
            weather_forecast,
            forecast_hours
        )
        
        # Generate predictions
        predictions = self._generate_predictions(features, forecast_hours)
        
        # Calculate confidence based on weather certainty and model variance
        confidence = self._calculate_confidence(weather_forecast, predictions)
        
        # Format output
        forecast = []
        current_time = datetime.utcnow()
        
        for i, pred in enumerate(predictions):
            forecast.append({
                'timestamp': (current_time + timedelta(hours=i+1)).isoformat(),
                'predicted_power': float(pred),  # Watts
                'predicted_energy': float(pred),  # Wh for 1 hour
                'confidence': float(confidence)
            })
        
        return {
            'predictions': forecast,
            'confidence': float(confidence),
            'metadata': {
                'model_version': '1.2.0',
                'features_used': self.features,
                'weather_source': 'OpenWeatherMap'
            }
        }
    
    async def _fetch_weather_forecast(
        self,
        lat: float,
        lon: float,
        hours: int
    ) -> List[Dict[str, Any]]:
        """Fetch weather forecast from external API"""
        # In production, use actual weather API (OpenWeatherMap, etc.)
        # This is a mock implementation
        
        forecast = []
        base_time = datetime.utcnow()
        
        for i in range(hours):
            # Generate synthetic forecast for demo
            hour = (base_time + timedelta(hours=i)).hour
            
            # Simple day/night cycle
            if 6 <= hour <= 18:
                irradiance = 800 * np.sin((hour - 6) * np.pi / 12)
                cloud_cover = np.random.uniform(0.1, 0.3)
            else:
                irradiance = 0
                cloud_cover = np.random.uniform(0.4, 0.8)
            
            forecast.append({
                'timestamp': (base_time + timedelta(hours=i)).isoformat(),
                'irradiance': irradiance,
                'cloud_cover': cloud_cover,
                'temperature': 25 + np.random.normal(0, 3),
                'humidity': np.random.uniform(40, 70),
                'wind_speed': np.random.uniform(0, 5)
            })
        
        return forecast
    
    def _prepare_features(
        self,
        historical_data: Optional[List[Dict[str, Any]]],
        weather_forecast: List[Dict[str, Any]],
        forecast_hours: int
    ) -> np.ndarray:
        """
        Prepare input features for the model
        Combines historical telemetry with weather forecast
        """
        # Lookback window (use last 24 hours of history)
        lookback = 24
        
        # Initialize feature matrix
        features = np.zeros((lookback + forecast_hours, len(self.features)))
        
        # Fill historical data
        if historical_data:
            for i, data in enumerate(historical_data[-lookback:]):
                features[i] = self._extract_features(data, is_historical=True)
        
        # Fill forecast data (using weather predictions)
        for i, weather in enumerate(weather_forecast):
            features[lookback + i] = self._extract_features(
                weather,
                is_historical=False
            )
        
        # Normalize features
        if self.scaler_params:
            features = (features - self.scaler_params['mean']) / self.scaler_params['std']
        
        return features
    
    def _extract_features(
        self,
        data: Dict[str, Any],
        is_historical: bool
    ) -> np.ndarray:
        """Extract feature vector from data point"""
        features = np.zeros(len(self.features))
        
        if is_historical:
            # Extract from telemetry
            solar = data.get('solar', {})
            battery = data.get('battery', {})
            
            features[0] = solar.get('panel_voltage', 0)
            features[1] = solar.get('panel_current', 0)
            features[2] = solar.get('panel_power', 0)
            features[3] = solar.get('irradiance', 0)
            features[4] = solar.get('panel_temp', 0)
            features[5] = battery.get('state_of_charge', 0)
        else:
            # Extract from weather forecast
            features[3] = data.get('irradiance', 0)
            features[9] = data.get('temperature', 25)
        
        # Time features
        ts = datetime.fromisoformat(data.get('timestamp', datetime.utcnow().isoformat()))
        features[6] = ts.hour
        features[7] = ts.timetuple().tm_yday
        features[8] = data.get('cloud_cover', 0)
        
        return features
    
    def _generate_predictions(
        self,
        features: np.ndarray,
        forecast_hours: int
    ) -> np.ndarray:
        """Run model inference"""
        predictions = []
        
        # Use sliding window for predictions
        lookback = 24
        
        for i in range(forecast_hours):
            # Extract window
            window = features[i:i+lookback]
            window = window.reshape(1, lookback, -1)  # (batch, seq, features)
            
            if self.use_onnx:
                # ONNX inference
                input_name = self.onnx_session.get_inputs()[0].name
                output = self.onnx_session.run(
                    None,
                    {input_name: window.astype(np.float32)}
                )[0]
                pred = output[0][0]
            else:
                # PyTorch inference
                with torch.no_grad():
                    window_tensor = torch.FloatTensor(window)
                    output = self.model(window_tensor)
                    pred = output.item()
            
            predictions.append(max(0, pred))  # Power cannot be negative
        
        # Denormalize predictions
        if self.scaler_params:
            predictions = np.array(predictions) * self.scaler_params['std'][2] + \
                         self.scaler_params['mean'][2]
        
        return predictions
    
    def _calculate_confidence(
        self,
        weather_forecast: List[Dict[str, Any]],
        predictions: np.ndarray
    ) -> float:
        """
        Calculate prediction confidence
        Based on weather forecast certainty and prediction variance
        """
        # Weather confidence (inversely related to cloud cover)
        avg_cloud_cover = np.mean([w['cloud_cover'] for w in weather_forecast])
        weather_confidence = 1.0 - (avg_cloud_cover * 0.5)
        
        # Prediction stability (low variance = high confidence)
        pred_variance = np.var(predictions) / (np.mean(predictions) + 1e-6)
        stability_confidence = 1.0 / (1.0 + pred_variance)
        
        # Combined confidence
        confidence = (weather_confidence + stability_confidence) / 2.0
        
        return np.clip(confidence, 0.5, 0.95)

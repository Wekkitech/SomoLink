"""
QoS (Quality of Service) Optimization Model
Uses contextual bandits for intelligent bandwidth allocation
"""
import numpy as np
import lightgbm as lgb
from typing import Dict, List, Optional
import pickle
import os


class QoSOptimizer:
    """
    Contextual Bandit-based QoS optimization for bandwidth allocation.
    
    Learns optimal bandwidth distribution across:
    - Educational content streaming
    - Web browsing
    - Downloads
    - Real-time collaboration
    """
    
    def __init__(self, model_path: Optional[str] = None):
        self.model_path = model_path
        self.model = None
        self.action_space = ['high_priority', 'medium_priority', 'low_priority', 'throttle']
        self.feature_names = [
            'hour_of_day',
            'day_of_week',
            'num_active_users',
            'available_bandwidth_mbps',
            'content_type',  # 0: edu, 1: browse, 2: download, 3: collab
            'user_tier',  # 0: free, 1: basic, 2: premium
            'battery_level',
            'solar_generation_w',
            'historical_usage_mb',
            'time_since_last_access_min'
        ]
        
        if model_path and os.path.exists(model_path):
            self.load_model(model_path)
        else:
            self._init_model()
    
    def _init_model(self):
        """Initialize LightGBM model for QoS prediction."""
        params = {
            'objective': 'multiclass',
            'num_class': len(self.action_space),
            'metric': 'multi_logloss',
            'boosting_type': 'gbdt',
            'num_leaves': 31,
            'learning_rate': 0.05,
            'feature_fraction': 0.9,
            'bagging_fraction': 0.8,
            'bagging_freq': 5,
            'verbose': -1
        }
        # Placeholder model (would be trained with historical data)
        self.model = lgb.LGBMClassifier(**params)
    
    def extract_features(self, context: Dict) -> np.ndarray:
        """
        Extract features from request context.
        
        Args:
            context: Dictionary containing:
                - timestamp
                - num_users
                - bandwidth_available
                - content_type
                - user_info
                - device_telemetry
        
        Returns:
            Feature vector for prediction
        """
        from datetime import datetime
        
        ts = datetime.fromisoformat(context.get('timestamp', datetime.now().isoformat()))
        
        features = [
            ts.hour,  # hour_of_day
            ts.weekday(),  # day_of_week
            context.get('num_active_users', 1),
            context.get('bandwidth_available_mbps', 10.0),
            self._encode_content_type(context.get('content_type', 'browse')),
            self._encode_user_tier(context.get('user_tier', 'free')),
            context.get('battery_level', 50),
            context.get('solar_generation_w', 0),
            context.get('historical_usage_mb', 0),
            context.get('time_since_last_access_min', 60)
        ]
        
        return np.array(features).reshape(1, -1)
    
    def _encode_content_type(self, content_type: str) -> int:
        """Encode content type to numeric."""
        mapping = {'educational': 0, 'browse': 1, 'download': 2, 'collaboration': 3}
        return mapping.get(content_type, 1)
    
    def _encode_user_tier(self, tier: str) -> int:
        """Encode user tier to numeric."""
        mapping = {'free': 0, 'basic': 1, 'premium': 2}
        return mapping.get(tier, 0)
    
    def recommend_action(self, context: Dict) -> Dict:
        """
        Recommend QoS action based on context.
        
        Args:
            context: Current network and user context
        
        Returns:
            Dictionary with recommended action and confidence
        """
        features = self.extract_features(context)
        
        if self.model is None:
            # Fallback to rule-based system if model not trained
            return self._rule_based_recommendation(context)
        
        try:
            # Predict action probabilities
            probabilities = self.model.predict_proba(features)[0]
            recommended_action_idx = np.argmax(probabilities)
            confidence = probabilities[recommended_action_idx]
            
            return {
                'action': self.action_space[recommended_action_idx],
                'confidence': float(confidence),
                'action_probabilities': {
                    action: float(prob) 
                    for action, prob in zip(self.action_space, probabilities)
                },
                'bandwidth_allocation_mbps': self._get_bandwidth_allocation(
                    self.action_space[recommended_action_idx],
                    context.get('bandwidth_available_mbps', 10.0)
                )
            }
        except Exception as e:
            print(f"Prediction error: {e}, falling back to rule-based")
            return self._rule_based_recommendation(context)
    
    def _rule_based_recommendation(self, context: Dict) -> Dict:
        """Fallback rule-based QoS recommendation."""
        content_type = context.get('content_type', 'browse')
        battery = context.get('battery_level', 50)
        bandwidth = context.get('bandwidth_available_mbps', 10.0)
        
        # Priority rules
        if content_type == 'educational':
            action = 'high_priority'
            allocation = bandwidth * 0.6
        elif content_type == 'collaboration':
            action = 'high_priority' if battery > 30 else 'medium_priority'
            allocation = bandwidth * 0.5
        elif content_type == 'browse':
            action = 'medium_priority'
            allocation = bandwidth * 0.3
        else:  # downloads
            action = 'low_priority' if battery > 40 else 'throttle'
            allocation = bandwidth * 0.2
        
        return {
            'action': action,
            'confidence': 0.7,
            'bandwidth_allocation_mbps': allocation,
            'method': 'rule_based'
        }
    
    def _get_bandwidth_allocation(self, action: str, total_bandwidth: float) -> float:
        """Calculate bandwidth allocation based on action."""
        allocations = {
            'high_priority': total_bandwidth * 0.6,
            'medium_priority': total_bandwidth * 0.3,
            'low_priority': total_bandwidth * 0.15,
            'throttle': total_bandwidth * 0.05
        }
        return allocations.get(action, total_bandwidth * 0.3)
    
    def update(self, context: Dict, action: str, reward: float):
        """
        Update model with observed reward (for online learning).
        
        Args:
            context: Context when action was taken
            action: Action that was taken
            reward: Observed reward (user satisfaction, completion rate, etc.)
        """
        # In production, this would store to a replay buffer
        # and periodically retrain the model
        pass
    
    def train(self, training_data: List[Dict]):
        """
        Train the QoS model on historical data.
        
        Args:
            training_data: List of dicts with 'context', 'action', 'reward'
        """
        if not training_data:
            return
        
        X = np.array([self.extract_features(d['context'])[0] for d in training_data])
        y = np.array([self.action_space.index(d['action']) for d in training_data])
        
        # Weight by reward (better outcomes get higher weight)
        sample_weights = np.array([d['reward'] for d in training_data])
        sample_weights = (sample_weights - sample_weights.min()) / (sample_weights.max() - sample_weights.min() + 1e-8)
        
        self.model.fit(X, y, sample_weight=sample_weights)
    
    def save_model(self, path: str):
        """Save model to disk."""
        with open(path, 'wb') as f:
            pickle.dump(self.model, f)
    
    def load_model(self, path: str):
        """Load model from disk."""
        with open(path, 'rb') as f:
            self.model = pickle.load(f)


# Export for API usage
__all__ = ['QoSOptimizer']

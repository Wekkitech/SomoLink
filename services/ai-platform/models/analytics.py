"""
Learning Analytics Model
Computes Connected Learning Hours (CLH) and generates educational insights
"""
import numpy as np
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Tuple
import pandas as pd
from collections import defaultdict


class LearningAnalytics:
    """
    Learning Analytics Engine for SomoLink.
    
    Tracks and analyzes:
    - Connected Learning Hours (CLH)
    - Student engagement patterns
    - Content effectiveness
    - Learning outcomes correlation
    - Resource utilization
    """
    
    def __init__(self):
        self.educational_domains = {
            'wikipedia.org', 'khanacademy.org', 'coursera.org',
            'edx.org', 'udemy.com', 'mit.edu', 'stanford.edu',
            '*.edu', 'google.com/scholar', 'researchgate.net',
            'arxiv.org', 'github.com', 'stackoverflow.com'
        }
        
        self.content_categories = {
            'educational_video': 1.0,
            'interactive_exercise': 1.2,
            'reading_material': 0.8,
            'assessment': 1.5,
            'collaboration': 1.3,
            'research': 1.1
        }
    
    def compute_clh(
        self,
        usage_logs: List[Dict],
        time_window_hours: int = 24
    ) -> Dict:
        """
        Compute Connected Learning Hours for a time window.
        
        CLH = Sum of (session_duration * quality_factor * engagement_score)
        
        Args:
            usage_logs: List of usage session logs
            time_window_hours: Time window to analyze
        
        Returns:
            CLH metrics and breakdown
        """
        now = datetime.now()
        cutoff_time = now - timedelta(hours=time_window_hours)
        
        # Filter relevant sessions
        relevant_sessions = [
            log for log in usage_logs
            if datetime.fromisoformat(log.get('timestamp', now.isoformat())) >= cutoff_time
        ]
        
        if not relevant_sessions:
            return {
                'clh_total': 0.0,
                'num_sessions': 0,
                'avg_session_quality': 0.0,
                'breakdown': {}
            }
        
        total_clh = 0.0
        category_clh = defaultdict(float)
        user_clh = defaultdict(float)
        
        for session in relevant_sessions:
            # Extract session info
            duration_minutes = session.get('duration_minutes', 0)
            user_id = session.get('user_id', 'unknown')
            content_type = session.get('content_type', 'other')
            domain = session.get('domain', '')
            
            # Calculate quality factor
            quality_factor = self._calculate_quality_factor(domain, content_type)
            
            # Calculate engagement score
            engagement_score = self._calculate_engagement_score(session)
            
            # Compute CLH for this session
            session_clh = (duration_minutes / 60.0) * quality_factor * engagement_score
            
            total_clh += session_clh
            category_clh[content_type] += session_clh
            user_clh[user_id] += session_clh
        
        return {
            'clh_total': round(total_clh, 2),
            'num_sessions': len(relevant_sessions),
            'num_unique_users': len(user_clh),
            'avg_session_quality': round(total_clh / len(relevant_sessions), 2),
            'time_window_hours': time_window_hours,
            'breakdown': {
                'by_category': dict(category_clh),
                'by_user': dict(user_clh),
                'top_users': self._get_top_users(user_clh, n=10)
            },
            'timestamp': now.isoformat()
        }
    
    def _calculate_quality_factor(self, domain: str, content_type: str) -> float:
        """
        Calculate quality factor based on content source and type.
        
        Returns: Float between 0.5 and 1.5
        """
        base_quality = 1.0
        
        # Check if domain is educational
        is_educational = any(
            edu_domain in domain.lower() 
            for edu_domain in self.educational_domains
        )
        
        if is_educational:
            base_quality *= 1.2
        
        # Apply content type multiplier
        content_multiplier = self.content_categories.get(content_type, 0.9)
        
        return min(base_quality * content_multiplier, 1.5)
    
    def _calculate_engagement_score(self, session: Dict) -> float:
        """
        Calculate engagement score based on session characteristics.
        
        Factors:
        - Interaction rate (clicks, scrolls, etc.)
        - Completion rate
        - Time of day (focused learning hours score higher)
        - Session continuity
        
        Returns: Float between 0.3 and 1.2
        """
        base_score = 1.0
        
        # Interaction rate
        interactions = session.get('num_interactions', 0)
        duration_minutes = max(session.get('duration_minutes', 1), 1)
        interaction_rate = interactions / duration_minutes
        
        if interaction_rate > 5:  # Highly engaged
            base_score *= 1.1
        elif interaction_rate < 1:  # Low engagement
            base_score *= 0.8
        
        # Completion rate
        completion = session.get('completion_rate', 0.5)
        base_score *= (0.7 + 0.3 * completion)
        
        # Time of day (school hours 8am-4pm score higher)
        timestamp = datetime.fromisoformat(session.get('timestamp', datetime.now().isoformat()))
        hour = timestamp.hour
        if 8 <= hour <= 16:  # School hours
            base_score *= 1.1
        elif hour >= 22 or hour <= 5:  # Late night/early morning
            base_score *= 0.9
        
        return np.clip(base_score, 0.3, 1.2)
    
    def _get_top_users(self, user_clh: Dict, n: int = 10) -> List[Dict]:
        """Get top N users by CLH."""
        sorted_users = sorted(
            user_clh.items(),
            key=lambda x: x[1],
            reverse=True
        )[:n]
        
        return [
            {'user_id': user_id, 'clh': round(clh, 2)}
            for user_id, clh in sorted_users
        ]
    
    def analyze_engagement_patterns(
        self,
        usage_logs: List[Dict],
        time_period_days: int = 7
    ) -> Dict:
        """
        Analyze engagement patterns over a time period.
        
        Args:
            usage_logs: Usage session logs
            time_period_days: Number of days to analyze
        
        Returns:
            Engagement analysis including trends and insights
        """
        now = datetime.now()
        cutoff = now - timedelta(days=time_period_days)
        
        # Filter and sort logs
        relevant_logs = sorted(
            [log for log in usage_logs 
             if datetime.fromisoformat(log.get('timestamp', now.isoformat())) >= cutoff],
            key=lambda x: x.get('timestamp', now.isoformat())
        )
        
        if not relevant_logs:
            return {'error': 'No data available for analysis'}
        
        # Aggregate by day and hour
        daily_stats = defaultdict(lambda: {'sessions': 0, 'clh': 0.0, 'users': set()})
        hourly_stats = defaultdict(lambda: {'sessions': 0, 'clh': 0.0})
        
        for log in relevant_logs:
            timestamp = datetime.fromisoformat(log.get('timestamp', now.isoformat()))
            date_key = timestamp.date().isoformat()
            hour_key = timestamp.hour
            
            duration = log.get('duration_minutes', 0) / 60.0
            quality = self._calculate_quality_factor(
                log.get('domain', ''),
                log.get('content_type', 'other')
            )
            engagement = self._calculate_engagement_score(log)
            clh = duration * quality * engagement
            
            daily_stats[date_key]['sessions'] += 1
            daily_stats[date_key]['clh'] += clh
            daily_stats[date_key]['users'].add(log.get('user_id', 'unknown'))
            
            hourly_stats[hour_key]['sessions'] += 1
            hourly_stats[hour_key]['clh'] += clh
        
        # Convert to lists for response
        daily_data = [
            {
                'date': date,
                'sessions': stats['sessions'],
                'clh': round(stats['clh'], 2),
                'unique_users': len(stats['users'])
            }
            for date, stats in sorted(daily_stats.items())
        ]
        
        hourly_data = [
            {
                'hour': hour,
                'sessions': stats['sessions'],
                'clh': round(stats['clh'], 2)
            }
            for hour, stats in sorted(hourly_stats.items())
        ]
        
        # Calculate trends
        clh_values = [d['clh'] for d in daily_data]
        clh_trend = 'increasing' if len(clh_values) > 1 and clh_values[-1] > clh_values[0] else 'stable'
        
        # Peak usage hours
        peak_hour = max(hourly_stats.items(), key=lambda x: x[1]['sessions'])[0]
        
        return {
            'time_period_days': time_period_days,
            'total_sessions': len(relevant_logs),
            'total_clh': round(sum(clh_values), 2),
            'avg_daily_clh': round(np.mean(clh_values), 2) if clh_values else 0,
            'clh_trend': clh_trend,
            'peak_usage_hour': peak_hour,
            'daily_breakdown': daily_data,
            'hourly_breakdown': hourly_data,
            'insights': self._generate_insights(daily_data, hourly_data)
        }
    
    def _generate_insights(self, daily_data: List[Dict], hourly_data: List[Dict]) -> List[str]:
        """Generate actionable insights from the data."""
        insights = []
        
        # Check for consistent usage
        if len(daily_data) >= 5:
            sessions_per_day = [d['sessions'] for d in daily_data]
            if np.std(sessions_per_day) < np.mean(sessions_per_day) * 0.3:
                insights.append("Consistent daily usage pattern - good engagement")
        
        # Check for growth
        if len(daily_data) >= 3:
            recent_clh = np.mean([d['clh'] for d in daily_data[-3:]])
            older_clh = np.mean([d['clh'] for d in daily_data[:3]])
            if recent_clh > older_clh * 1.2:
                insights.append("Learning hours increasing - positive trend")
        
        # Check peak hours
        peak_hours = sorted(hourly_data, key=lambda x: x['sessions'], reverse=True)[:3]
        peak_hour_numbers = [h['hour'] for h in peak_hours]
        if all(8 <= h <= 16 for h in peak_hour_numbers):
            insights.append("Peak usage during school hours - aligned with learning objectives")
        
        # Check weekend vs weekday (if we have that data)
        if len(daily_data) >= 7:
            insights.append("Sufficient data for weekly pattern analysis")
        
        return insights
    
    def compute_content_effectiveness(
        self,
        usage_logs: List[Dict],
        assessment_results: Optional[List[Dict]] = None
    ) -> Dict:
        """
        Analyze content effectiveness based on usage and outcomes.
        
        Args:
            usage_logs: Content access logs
            assessment_results: Optional assessment/quiz results
        
        Returns:
            Content effectiveness metrics
        """
        content_stats = defaultdict(lambda: {
            'views': 0,
            'total_duration': 0,
            'completions': 0,
            'avg_engagement': []
        })
        
        for log in usage_logs:
            content_id = log.get('content_id', 'unknown')
            duration = log.get('duration_minutes', 0)
            completion = log.get('completion_rate', 0)
            engagement = self._calculate_engagement_score(log)
            
            content_stats[content_id]['views'] += 1
            content_stats[content_id]['total_duration'] += duration
            if completion >= 0.8:  # 80% completion threshold
                content_stats[content_id]['completions'] += 1
            content_stats[content_id]['avg_engagement'].append(engagement)
        
        # Compile results
        effectiveness_scores = []
        for content_id, stats in content_stats.items():
            if stats['views'] == 0:
                continue
            
            completion_rate = stats['completions'] / stats['views']
            avg_engagement = np.mean(stats['avg_engagement']) if stats['avg_engagement'] else 0
            avg_duration = stats['total_duration'] / stats['views']
            
            # Overall effectiveness score
            effectiveness = (
                completion_rate * 0.4 +
                avg_engagement * 0.4 +
                min(avg_duration / 30, 1.0) * 0.2  # Normalize to 30 min
            )
            
            effectiveness_scores.append({
                'content_id': content_id,
                'views': stats['views'],
                'completion_rate': round(completion_rate, 2),
                'avg_engagement': round(avg_engagement, 2),
                'avg_duration_minutes': round(avg_duration, 1),
                'effectiveness_score': round(effectiveness, 2)
            })
        
        # Sort by effectiveness
        effectiveness_scores.sort(key=lambda x: x['effectiveness_score'], reverse=True)
        
        return {
            'total_content_items': len(content_stats),
            'content_rankings': effectiveness_scores,
            'top_content': effectiveness_scores[:10],
            'underperforming_content': [
                c for c in effectiveness_scores 
                if c['effectiveness_score'] < 0.5
            ][:10]
        }
    
    def generate_student_report(
        self,
        user_id: str,
        usage_logs: List[Dict],
        days: int = 30
    ) -> Dict:
        """
        Generate individual student learning report.
        
        Args:
            user_id: Student identifier
            usage_logs: Usage logs
            days: Report period in days
        
        Returns:
            Comprehensive student report
        """
        # Filter user's logs
        user_logs = [
            log for log in usage_logs
            if log.get('user_id') == user_id
        ]
        
        # Compute metrics
        clh_result = self.compute_clh(user_logs, time_window_hours=days*24)
        
        # Active days
        dates = set(
            datetime.fromisoformat(log.get('timestamp', datetime.now().isoformat())).date()
            for log in user_logs
        )
        
        # Subject breakdown
        subjects = defaultdict(float)
        for log in user_logs:
            subject = log.get('subject', 'General')
            duration = log.get('duration_minutes', 0) / 60.0
            subjects[subject] += duration
        
        return {
            'user_id': user_id,
            'report_period_days': days,
            'total_clh': clh_result['clh_total'],
            'active_days': len(dates),
            'avg_daily_clh': round(clh_result['clh_total'] / max(days, 1), 2),
            'total_sessions': len(user_logs),
            'subject_breakdown': dict(subjects),
            'engagement_level': self._classify_engagement(
                clh_result['clh_total'],
                len(dates),
                days
            ),
            'recommendations': self._generate_student_recommendations(
                clh_result,
                len(dates),
                days
            )
        }
    
    def _classify_engagement(self, total_clh: float, active_days: int, period_days: int) -> str:
        """Classify student engagement level."""
        avg_daily_clh = total_clh / max(period_days, 1)
        activity_rate = active_days / max(period_days, 1)
        
        if avg_daily_clh >= 2.0 and activity_rate >= 0.7:
            return 'highly_engaged'
        elif avg_daily_clh >= 1.0 and activity_rate >= 0.5:
            return 'engaged'
        elif avg_daily_clh >= 0.5 and activity_rate >= 0.3:
            return 'moderately_engaged'
        else:
            return 'needs_attention'
    
    def _generate_student_recommendations(
        self,
        clh_data: Dict,
        active_days: int,
        period_days: int
    ) -> List[str]:
        """Generate personalized recommendations for student."""
        recommendations = []
        
        activity_rate = active_days / max(period_days, 1)
        
        if activity_rate < 0.3:
            recommendations.append("Increase learning frequency - aim for daily sessions")
        
        avg_quality = clh_data.get('avg_session_quality', 0)
        if avg_quality < 0.5:
            recommendations.append("Focus on higher-quality educational content")
        
        if clh_data.get('clh_total', 0) < period_days * 0.5:
            recommendations.append("Increase daily learning time to at least 30 minutes")
        
        if not recommendations:
            recommendations.append("Keep up the great work! Consider exploring new subjects")
        
        return recommendations


__all__ = ['LearningAnalytics']

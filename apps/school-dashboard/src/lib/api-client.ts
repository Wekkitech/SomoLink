import axios from 'axios';

const API_GATEWAY_URL = process.env.NEXT_PUBLIC_API_GATEWAY_URL || 'http://localhost:3001';
const AI_PLATFORM_URL = process.env.NEXT_PUBLIC_AI_PLATFORM_URL || 'http://localhost:8000';

// Create axios instance for API Gateway
export const apiClient = axios.create({
  baseURL: API_GATEWAY_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Create axios instance for AI Platform
export const aiClient = axios.create({
  baseURL: AI_PLATFORM_URL,
  timeout: 30000, // Longer timeout for ML operations
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API functions
export const deviceApi = {
  getAll: () => apiClient.get('/api/devices'),
  getById: (id: string) => apiClient.get(`/api/devices/${id}`),
  getTelemetry: (id: string, period?: string) => 
    apiClient.get(`/api/devices/${id}/telemetry`, { params: { period } }),
};

export const analyticsApi = {
  getCLH: (params: any) => aiClient.post('/api/v1/analytics/clh', params),
  getEngagement: (params: any) => aiClient.post('/api/v1/analytics/engagement', params),
  getStudentReport: (userId: string, days: number) => 
    aiClient.post('/api/v1/analytics/student-report', { user_id: userId, days, usage_logs: [] }),
};

export const mlApi = {
  forecastSolar: (deviceId: string, location: any) => 
    aiClient.post('/api/v1/solar/forecast', { device_id: deviceId, location }),
  recommendQoS: (context: any) => 
    aiClient.post('/api/v1/qos/recommend', context),
  detectAnomaly: (deviceId: string, telemetry: any) => 
    aiClient.post('/api/v1/anomaly/detect', { device_id: deviceId, telemetry, detection_type: 'point' }),
};

export default apiClient;

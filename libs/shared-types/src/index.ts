/**
 * Shared TypeScript types for SomoLink platform
 * Used across all frontend and backend TypeScript services
 */

// ============== Device Types ==============

export interface Device {
  id: string;
  name: string;
  location: Location;
  status: DeviceStatus;
  telemetry: DeviceTelemetry;
  createdAt: string;
  updatedAt: string;
}

export type DeviceStatus = 'online' | 'offline' | 'critical' | 'maintenance';

export interface Location {
  latitude: number;
  longitude: number;
  address?: string;
  schoolName?: string;
  region?: string;
}

export interface DeviceTelemetry {
  timestamp: string;
  battery: BatteryMetrics;
  solar: SolarMetrics;
  network: NetworkMetrics;
  system: SystemMetrics;
}

export interface BatteryMetrics {
  soc: number; // State of charge (0-100)
  voltage: number;
  current: number;
  temperature: number;
  health: number; // 0-100
}

export interface SolarMetrics {
  voltage: number;
  current: number;
  power: number;
  generation24h: number;
}

export interface NetworkMetrics {
  signalStrength: number;
  bandwidth: number;
  activeUsers: number;
  dataConsumed: number;
  packetLoss: number;
  latency: number;
}

export interface SystemMetrics {
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
  temperature: number;
  uptime: number;
}

// ============== User Types ==============

export interface User {
  id: string;
  username: string;
  email: string;
  role: UserRole;
  profile: UserProfile;
  createdAt: string;
}

export type UserRole = 'admin' | 'teacher' | 'student' | 'guest';

export interface UserProfile {
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  schoolId?: string;
  grade?: string;
}

// ============== Analytics Types ==============

export interface CLHMetrics {
  total: number;
  byCategory: Record<string, number>;
  byUser: Record<string, number>;
  timeWindow: string;
  timestamp: string;
}

export interface EngagementMetrics {
  dailyStats: DailyEngagement[];
  hourlyStats: HourlyEngagement[];
  trends: EngagementTrends;
}

export interface DailyEngagement {
  date: string;
  sessions: number;
  clh: number;
  uniqueUsers: number;
}

export interface HourlyEngagement {
  hour: number;
  sessions: number;
  clh: number;
}

export interface EngagementTrends {
  clhTrend: 'increasing' | 'decreasing' | 'stable';
  peakHour: number;
  avgDailyCLH: number;
}

export interface StudentReport {
  userId: string;
  reportPeriod: number;
  totalCLH: number;
  activeDays: number;
  avgDailyCLH: number;
  totalSessions: number;
  subjectBreakdown: Record<string, number>;
  engagementLevel: EngagementLevel;
  recommendations: string[];
}

export type EngagementLevel = 'highly_engaged' | 'engaged' | 'moderately_engaged' | 'needs_attention';

// ============== ML Model Types ==============

export interface SolarForecast {
  deviceId: string;
  hourlyForecast: HourlyForecast[];
  confidence: number;
  generatedAt: string;
}

export interface HourlyForecast {
  hour: number;
  generation: number;
  batterySoC: number;
}

export interface QoSRecommendation {
  action: 'high_priority' | 'medium_priority' | 'low_priority' | 'throttle';
  confidence: number;
  bandwidthAllocation: number;
  actionProbabilities: Record<string, number>;
}

export interface AnomalyDetection {
  deviceId: string;
  isAnomaly: boolean;
  severity: 'low' | 'medium' | 'high' | 'critical';
  flaggedMetrics: string[];
  confidence: number;
  recommendedAction: string;
  timestamp: string;
}

// ============== API Response Types ==============

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
  timestamp: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: any;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

// ============== Billing Types ==============

export interface Payment {
  id: string;
  userId: string;
  amount: number;
  currency: string;
  method: PaymentMethod;
  status: PaymentStatus;
  transactionId?: string;
  createdAt: string;
}

export type PaymentMethod = 'mpesa' | 'card' | 'cash';
export type PaymentStatus = 'pending' | 'completed' | 'failed' | 'refunded';

export interface Subscription {
  id: string;
  userId: string;
  plan: SubscriptionPlan;
  status: SubscriptionStatus;
  startDate: string;
  endDate: string;
  autoRenew: boolean;
}

export type SubscriptionPlan = 'free' | 'basic' | 'premium';
export type SubscriptionStatus = 'active' | 'inactive' | 'cancelled' | 'expired';

// ============== Event Types ==============

export interface TelemetryEvent {
  deviceId: string;
  type: 'telemetry';
  data: DeviceTelemetry;
  timestamp: string;
}

export interface UsageEvent {
  deviceId: string;
  userId: string;
  type: 'usage';
  sessionId: string;
  duration: number;
  dataConsumed: number;
  timestamp: string;
}

export interface AlertEvent {
  deviceId: string;
  type: 'alert';
  severity: 'info' | 'warning' | 'error' | 'critical';
  message: string;
  data?: any;
  timestamp: string;
}

export type SystemEvent = TelemetryEvent | UsageEvent | AlertEvent;

// ============== Configuration Types ==============

export interface AppConfig {
  apiUrl: string;
  wsUrl: string;
  environment: 'development' | 'staging' | 'production';
  features: FeatureFlags;
}

export interface FeatureFlags {
  enableMLFeatures: boolean;
  enablePayments: boolean;
  enableAnalytics: boolean;
  enableNotifications: boolean;
}

// ============== Export all types ==============

export type {
  // Re-export for convenience
  Device as DeviceType,
  User as UserType,
  Payment as PaymentType,
  Subscription as SubscriptionType,
};

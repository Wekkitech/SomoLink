export default () => ({
  port: parseInt(process.env.PORT, 10) || 3001,
  environment: process.env.NODE_ENV || 'development',
  
  // Database
  database: {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT, 10) || 5432,
    username: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || 'postgres',
    database: process.env.DB_NAME || 'somolink',
  },

  // JWT
  jwt: {
    secret: process.env.JWT_SECRET || 'your-secret-key-change-in-production',
    expiresIn: process.env.JWT_EXPIRES_IN || '1h',
    refreshExpiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d',
  },

  // Redis
  redis: {
    host: process.env.REDIS_HOST || 'localhost',
    port: parseInt(process.env.REDIS_PORT, 10) || 6379,
    password: process.env.REDIS_PASSWORD || '',
  },

  // Services
  services: {
    aiPlatform: {
      url: process.env.AI_PLATFORM_URL || 'http://localhost:8000',
      timeout: 30000,
    },
    billing: {
      url: process.env.BILLING_URL || 'http://localhost:3003',
      timeout: 10000,
    },
    dataIngestion: {
      url: process.env.DATA_INGESTION_URL || 'http://localhost:8001',
      timeout: 5000,
    },
  },

  // Security
  security: {
    rateLimitWindow: parseInt(process.env.RATE_LIMIT_WINDOW, 10) || 60000, // 1 minute
    rateLimitMax: parseInt(process.env.RATE_LIMIT_MAX, 10) || 100,
    corsOrigins: process.env.CORS_ORIGINS?.split(',') || ['http://localhost:3000'],
  },

  // Monitoring
  monitoring: {
    enablePrometheus: process.env.ENABLE_PROMETHEUS === 'true',
    metricsPath: process.env.METRICS_PATH || '/metrics',
  },
});

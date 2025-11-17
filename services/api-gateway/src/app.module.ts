import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { ThrottlerModule } from '@nestjs/throttler';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { PrometheusModule } from '@willsoto/nestjs-prometheus';
import { ClientsModule, Transport } from '@nestjs/microservices';

import { AuthModule } from './auth/auth.module';
import { DevicesModule } from './routes/devices/devices.module';
import { TelemetryModule } from './routes/telemetry/telemetry.module';
import { AnalyticsModule } from './routes/analytics/analytics.module';
import { BillingModule } from './routes/billing/billing.module';
import { HealthController } from './health/health.controller';
import { LoggingMiddleware } from './middleware/logging.middleware';
import { JwtAuthGuard } from './auth/guards/jwt-auth.guard';
import { RolesGuard } from './auth/guards/roles.guard';

@Module({
  imports: [
    // Configuration
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env.local', '.env'],
    }),

    // Rate limiting
    ThrottlerModule.forRoot([{
      ttl: 60000, // 1 minute
      limit: 100, // 100 requests per minute
    }]),

    // JWT Authentication
    JwtModule.registerAsync({
      imports: [ConfigModule],
      useFactory: async (configService: ConfigService) => ({
        secret: configService.get<string>('JWT_SECRET'),
        signOptions: {
          expiresIn: configService.get<string>('JWT_EXPIRES_IN', '1h'),
        },
      }),
      inject: [ConfigService],
    }),

    PassportModule.register({ defaultStrategy: 'jwt' }),

    // Prometheus metrics
    PrometheusModule.register({
      defaultMetrics: {
        enabled: true,
      },
      path: '/metrics',
    }),

    // Kafka client for event streaming
    ClientsModule.registerAsync([
      {
        name: 'TELEMETRY_SERVICE',
        imports: [ConfigModule],
        useFactory: (configService: ConfigService) => ({
          transport: Transport.KAFKA,
          options: {
            client: {
              clientId: 'api-gateway',
              brokers: configService.get<string>('KAFKA_BROKERS', 'localhost:9092').split(','),
            },
            consumer: {
              groupId: 'api-gateway-consumer',
            },
          },
        }),
        inject: [ConfigService],
      },
    ]),

    // gRPC client for AI platform
    ClientsModule.registerAsync([
      {
        name: 'AI_PLATFORM',
        imports: [ConfigModule],
        useFactory: (configService: ConfigService) => ({
          transport: Transport.GRPC,
          options: {
            package: 'somolink.ai',
            protoPath: join(__dirname, '../../../proto/ai-platform.proto'),
            url: configService.get<string>('AI_PLATFORM_URL', 'localhost:50051'),
          },
        }),
        inject: [ConfigService],
      },
    ]),

    // Feature modules
    AuthModule,
    DevicesModule,
    TelemetryModule,
    AnalyticsModule,
    BillingModule,
  ],
  controllers: [HealthController],
  providers: [
    // Global guards
    {
      provide: 'APP_GUARD',
      useClass: JwtAuthGuard,
    },
    {
      provide: 'APP_GUARD',
      useClass: RolesGuard,
    },
  ],
})
export class AppModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(LoggingMiddleware)
      .forRoutes('*');
  }
}

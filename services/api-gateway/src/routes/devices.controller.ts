import { Controller, Get, Post, Param, Query, UseGuards, HttpService } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller('api/devices')
@UseGuards(JwtAuthGuard)
export class DevicesController {
  constructor(private readonly httpService: HttpService) {}

  @Get()
  async getAllDevices(@Query('status') status?: string) {
    // Proxy to internal services or database
    return {
      success: true,
      data: [
        {
          id: 'dev-001',
          name: 'Nairobi Primary School',
          status: 'online',
          location: {
            latitude: -1.286389,
            longitude: 36.817223,
          },
        },
      ],
      timestamp: new Date().toISOString(),
    };
  }

  @Get(':id')
  async getDeviceById(@Param('id') id: string) {
    return {
      success: true,
      data: {
        id: id,
        name: `Device ${id}`,
        status: 'online',
        telemetry: {
          battery: { soc: 85, voltage: 12.4, current: 5.2 },
          solar: { power: 150, generation24h: 3600 },
          network: { activeUsers: 45, bandwidth: 100 },
        },
      },
      timestamp: new Date().toISOString(),
    };
  }

  @Get(':id/telemetry')
  async getDeviceTelemetry(
    @Param('id') id: string,
    @Query('period') period: string = '24h'
  ) {
    return {
      success: true,
      data: {
        deviceId: id,
        period: period,
        telemetry: [
          // Mock telemetry data
          {
            timestamp: new Date().toISOString(),
            battery: { soc: 85 },
            solar: { power: 150 },
          },
        ],
      },
      timestamp: new Date().toISOString(),
    };
  }

  @Post(':id/command')
  async sendCommand(
    @Param('id') id: string,
    @Query('command') command: string
  ) {
    return {
      success: true,
      data: {
        deviceId: id,
        command: command,
        status: 'sent',
      },
      timestamp: new Date().toISOString(),
    };
  }
}

'use client';

import { useState, useEffect } from 'react';
import { 
  Battery, Wifi, Sun, Users, BookOpen, TrendingUp, 
  AlertCircle, Activity, Download 
} from 'lucide-react';
import { 
  LineChart, Line, BarChart, Bar, AreaChart, Area,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts';

interface TelemetryData {
  deviceId: string;
  timestamp: string;
  solar: {
    panelPower: number;
    irradiance: number;
    panelTemp: number;
  };
  battery: {
    voltage: number;
    stateOfCharge: number;
    health: number;
  };
  network: {
    signalStrength: number;
    bandwidth: number;
    connectedUsers: number;
  };
  learning: {
    activeSessions: number;
    connectedLearningHours: number;
    cacheHitRate: number;
  };
}

interface SolarForecast {
  timestamp: string;
  predictedPower: number;
  confidence: number;
}

export default function SchoolDashboard() {
  const [telemetry, setTelemetry] = useState<TelemetryData | null>(null);
  const [solarForecast, setSolarForecast] = useState<SolarForecast[]>([]);
  const [learningStats, setLearningStats] = useState<any>(null);
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch initial data
    fetchDashboardData();
    
    // Set up real-time updates every 30 seconds
    const interval = setInterval(fetchDashboardData, 30000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchDashboardData = async () => {
    try {
      // In production, these would be actual API calls
      const [telemetryRes, forecastRes, statsRes, alertsRes] = await Promise.all([
        fetch('/api/telemetry/latest'),
        fetch('/api/solar/forecast'),
        fetch('/api/learning/stats'),
        fetch('/api/alerts/active')
      ]);

      const telemetryData = await telemetryRes.json();
      const forecastData = await forecastRes.json();
      const statsData = await statsRes.json();
      const alertsData = await alertsRes.json();

      setTelemetry(telemetryData);
      setSolarForecast(forecastData.forecast);
      setLearningStats(statsData);
      setAlerts(alertsData);
      setLoading(false);
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
      // Use mock data for demo
      setMockData();
    }
  };

  const setMockData = () => {
    setTelemetry({
      deviceId: 'SLNK-001-KE',
      timestamp: new Date().toISOString(),
      solar: {
        panelPower: 145.5,
        irradiance: 750,
        panelTemp: 42
      },
      battery: {
        voltage: 12.6,
        stateOfCharge: 78,
        health: 95
      },
      network: {
        signalStrength: -72,
        bandwidth: 5.2,
        connectedUsers: 24
      },
      learning: {
        activeSessions: 12,
        connectedLearningHours: 156.5,
        cacheHitRate: 82
      }
    });

    setSolarForecast(
      Array.from({ length: 24 }, (_, i) => ({
        timestamp: new Date(Date.now() + i * 3600000).toLocaleTimeString('en-US', { 
          hour: '2-digit' 
        }),
        predictedPower: Math.max(0, 200 * Math.sin((i - 6) * Math.PI / 12)),
        confidence: 0.85
      }))
    );

    setAlerts([
      { 
        id: 1, 
        severity: 'warning', 
        message: 'Battery health declining - schedule maintenance',
        timestamp: new Date(Date.now() - 3600000).toISOString()
      }
    ]);

    setLoading(false);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="text-center">
          <Activity className="w-12 h-12 animate-spin text-blue-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  const getBatteryColor = (soc: number) => {
    if (soc >= 70) return 'text-green-600';
    if (soc >= 40) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getSignalColor = (strength: number) => {
    if (strength >= -70) return 'text-green-600';
    if (strength >= -85) return 'text-yellow-600';
    return 'text-red-600';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                SomoLink School Dashboard
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                Device: {telemetry?.deviceId} | Last updated: {new Date(telemetry?.timestamp || '').toLocaleTimeString()}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2">
                <Download className="w-4 h-4" />
                Export Report
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Alerts */}
        {alerts.length > 0 && (
          <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-yellow-600 mt-0.5" />
              <div className="flex-1">
                <h3 className="font-semibold text-yellow-900">Active Alerts</h3>
                {alerts.map(alert => (
                  <p key={alert.id} className="text-sm text-yellow-800 mt-1">
                    {alert.message}
                  </p>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Status Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Solar Power Card */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-gray-600">Solar Power</h3>
              <Sun className="w-5 h-5 text-yellow-500" />
            </div>
            <div className="space-y-2">
              <p className="text-3xl font-bold text-gray-900">
                {telemetry?.solar.panelPower.toFixed(1)} W
              </p>
              <p className="text-sm text-gray-500">
                Irradiance: {telemetry?.solar.irradiance} W/m²
              </p>
            </div>
          </div>

          {/* Battery Card */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-gray-600">Battery</h3>
              <Battery className={`w-5 h-5 ${getBatteryColor(telemetry?.battery.stateOfCharge || 0)}`} />
            </div>
            <div className="space-y-2">
              <p className={`text-3xl font-bold ${getBatteryColor(telemetry?.battery.stateOfCharge || 0)}`}>
                {telemetry?.battery.stateOfCharge}%
              </p>
              <p className="text-sm text-gray-500">
                Health: {telemetry?.battery.health}%
              </p>
            </div>
          </div>

          {/* Network Card */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-gray-600">Network</h3>
              <Wifi className={`w-5 h-5 ${getSignalColor(telemetry?.network.signalStrength || 0)}`} />
            </div>
            <div className="space-y-2">
              <p className="text-3xl font-bold text-gray-900">
                {telemetry?.network.bandwidth.toFixed(1)} Mbps
              </p>
              <p className="text-sm text-gray-500">
                Signal: {telemetry?.network.signalStrength} dBm
              </p>
            </div>
          </div>

          {/* Active Users Card */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-gray-600">Active Users</h3>
              <Users className="w-5 h-5 text-blue-500" />
            </div>
            <div className="space-y-2">
              <p className="text-3xl font-bold text-gray-900">
                {telemetry?.network.connectedUsers}
              </p>
              <p className="text-sm text-gray-500">
                Sessions: {telemetry?.learning.activeSessions}
              </p>
            </div>
          </div>
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {/* Solar Forecast Chart */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Sun className="w-5 h-5 text-yellow-500" />
              24-Hour Solar Forecast
            </h3>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={solarForecast}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" />
                <YAxis label={{ value: 'Power (W)', angle: -90, position: 'insideLeft' }} />
                <Tooltip />
                <Area 
                  type="monotone" 
                  dataKey="predictedPower" 
                  stroke="#f59e0b" 
                  fill="#fef3c7" 
                  name="Predicted Power (W)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>

          {/* Learning Metrics Chart */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-blue-500" />
              Connected Learning Hours (CLH)
            </h3>
            <div className="space-y-4">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-700">Today</span>
                  <span className="text-2xl font-bold text-blue-600">
                    {telemetry?.learning.connectedLearningHours.toFixed(1)} hrs
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-blue-600 h-2 rounded-full" 
                    style={{ width: `${Math.min((telemetry?.learning.connectedLearningHours || 0) / 200 * 100, 100)}%` }}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                <div>
                  <p className="text-sm text-gray-600">Cache Hit Rate</p>
                  <p className="text-xl font-bold text-green-600">
                    {telemetry?.learning.cacheHitRate}%
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Active Sessions</p>
                  <p className="text-xl font-bold text-blue-600">
                    {telemetry?.learning.activeSessions}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Learning Analytics Table */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-green-500" />
            Recent Activity
          </h3>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead>
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Students
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Content Accessed
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    CLH
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {Array.from({ length: 5 }, (_, i) => (
                  <tr key={i}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(Date.now() - i * 3600000).toLocaleTimeString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {15 + Math.floor(Math.random() * 10)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      Math, Science, English
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-blue-600">
                      {(20 + Math.random() * 15).toFixed(1)} hrs
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}

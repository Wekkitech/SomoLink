interface DeviceCardProps {
  device: {
    id: string;
    name: string;
    status: 'online' | 'offline' | 'critical';
    batteryLevel: number;
    solarGeneration: number;
    activeUsers: number;
    location: string;
  };
  onClick?: () => void;
}

export default function DeviceCard({ device, onClick }: DeviceCardProps) {
  const statusColors = {
    online: 'bg-green-100 text-green-800 border-green-200',
    offline: 'bg-gray-100 text-gray-800 border-gray-200',
    critical: 'bg-red-100 text-red-800 border-red-200',
  };

  const batteryColor = 
    device.batteryLevel > 60 ? 'text-green-600' :
    device.batteryLevel > 30 ? 'text-yellow-600' :
    'text-red-600';

  return (
    <div 
      className="bg-white rounded-lg shadow-md p-6 border-2 border-gray-100 hover:shadow-lg transition-shadow cursor-pointer"
      onClick={onClick}
    >
      {/* Header */}
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">{device.name}</h3>
          <p className="text-sm text-gray-500">{device.location}</p>
        </div>
        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors[device.status]}`}>
          {device.status.toUpperCase()}
        </span>
      </div>

      {/* Metrics */}
      <div className="space-y-3">
        {/* Battery */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <svg className={`w-5 h-5 ${batteryColor}`} fill="currentColor" viewBox="0 0 20 20">
              <path d="M11 3a1 1 0 10-2 0v1a1 1 0 102 0V3zM15.657 5.757a1 1 0 00-1.414-1.414l-.707.707a1 1 0 001.414 1.414l.707-.707zM18 10a1 1 0 01-1 1h-1a1 1 0 110-2h1a1 1 0 011 1zM5.05 6.464A1 1 0 106.464 5.05l-.707-.707a1 1 0 00-1.414 1.414l.707.707zM5 10a1 1 0 01-1 1H3a1 1 0 110-2h1a1 1 0 011 1zM8 16v-1h4v1a2 2 0 11-4 0zM12 14c.015-.34.208-.646.477-.859a4 4 0 10-4.954 0c.27.213.462.519.476.859h4.002z"/>
            </svg>
            <span className="text-sm text-gray-600">Battery</span>
          </div>
          <span className={`font-semibold ${batteryColor}`}>{device.batteryLevel}%</span>
        </div>

        {/* Solar Generation */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <svg className="w-5 h-5 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clipRule="evenodd"/>
            </svg>
            <span className="text-sm text-gray-600">Solar</span>
          </div>
          <span className="font-semibold text-gray-900">{device.solarGeneration}W</span>
        </div>

        {/* Active Users */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <svg className="w-5 h-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
            </svg>
            <span className="text-sm text-gray-600">Users</span>
          </div>
          <span className="font-semibold text-gray-900">{device.activeUsers}</span>
        </div>
      </div>

      {/* Footer */}
      <div className="mt-4 pt-4 border-t border-gray-100">
        <button className="text-sm text-blue-600 hover:text-blue-800 font-medium">
          View Details →
        </button>
      </div>
    </div>
  );
}

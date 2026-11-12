import { useEffect, useState } from 'react';
import { Users, Car, Calendar, ClipboardList, FileText, UserCog, Building2, TrendingUp } from 'lucide-react';
import { customerApi, vehicleApi, bookingApi, jobApi, technicianApi, serviceBayApi } from '../api/services';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

interface DashboardStats {
  totalCustomers: number;
  totalVehicles: number;
  totalBookings: number;
  activeJobs: number;
  availableTechnicians: number;
  activeServiceBays: number;
}

const COLORS = ['#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch data from all endpoints
      const [customers, vehicles, bookings, jobs, technicians, bays] = await Promise.all([
        customerApi.getAll({ size: 1 }),
        vehicleApi.getAll({ size: 1 }),
        bookingApi.getAll({ size: 1 }),
        jobApi.getAll({ size: 1 }),
        technicianApi.getAll({ size: 1, activeOnly: true }),
        serviceBayApi.getAll({ size: 1, activeOnly: true }),
      ]);

      setStats({
        totalCustomers: customers.data.totalElements,
        totalVehicles: vehicles.data.totalElements,
        totalBookings: bookings.data.totalElements,
        activeJobs: jobs.data.totalElements,
        availableTechnicians: technicians.data.totalElements,
        activeServiceBays: bays.data.totalElements,
      });
    } catch (err: any) {
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} onRetry={loadDashboardData} />;
  if (!stats) return null;

  const statCards = [
    { label: 'Total Customers', value: stats.totalCustomers, icon: Users, color: 'bg-blue-500' },
    { label: 'Total Vehicles', value: stats.totalVehicles, icon: Car, color: 'bg-green-500' },
    { label: 'Bookings', value: stats.totalBookings, icon: Calendar, color: 'bg-yellow-500' },
    { label: 'Active Jobs', value: stats.activeJobs, icon: ClipboardList, color: 'bg-red-500' },
    { label: 'Available Technicians', value: stats.availableTechnicians, icon: UserCog, color: 'bg-purple-500' },
    { label: 'Active Service Bays', value: stats.activeServiceBays, icon: Building2, color: 'bg-pink-500' },
  ];

  // Mock data for charts (would be replaced with real data)
  const bookingTrends = [
    { name: 'Mon', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.15) : 0 },
    { name: 'Tue', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.18) : 0 },
    { name: 'Wed', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.14) : 0 },
    { name: 'Thu', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.20) : 0 },
    { name: 'Fri', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.17) : 0 },
    { name: 'Sat', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.10) : 0 },
    { name: 'Sun', bookings: stats.totalBookings > 0 ? Math.floor(stats.totalBookings * 0.06) : 0 },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-gray-900">Dashboard</h2>
          <p className="text-gray-600 mt-1">Overview of your service center operations</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6">
        {statCards.map((stat) => {
          const Icon = stat.icon;
          return (
            <div key={stat.label} className="card">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">{stat.label}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
                </div>
                <div className={`${stat.color} p-3 rounded-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Booking Trends */}
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Weekly Booking Distribution</h3>
          {stats.totalBookings > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={bookingTrends}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="bookings" fill="#0ea5e9" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center text-gray-500">
              No booking data available
            </div>
          )}
        </div>

        {/* Resource Utilization */}
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Resource Overview</h3>
          {stats.totalCustomers > 0 || stats.totalVehicles > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={[
                    { name: 'Customers', value: stats.totalCustomers },
                    { name: 'Vehicles', value: stats.totalVehicles },
                    { name: 'Bookings', value: stats.totalBookings },
                    { name: 'Jobs', value: stats.activeJobs },
                    { name: 'Technicians', value: stats.availableTechnicians },
                    { name: 'Bays', value: stats.activeServiceBays },
                  ]}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({name, percent}) => percent ? `${name}: ${(percent * 100).toFixed(0)}%` : name}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {statCards.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center text-gray-500">
              No data available yet
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <a href="/customers/new" className="p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors">
            <Users className="w-8 h-8 text-primary-600 mb-2" />
            <p className="font-medium text-gray-900">Add Customer</p>
          </a>
          <a href="/vehicles/new" className="p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors">
            <Car className="w-8 h-8 text-primary-600 mb-2" />
            <p className="font-medium text-gray-900">Register Vehicle</p>
          </a>
          <a href="/bookings/new" className="p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors">
            <Calendar className="w-8 h-8 text-primary-600 mb-2" />
            <p className="font-medium text-gray-900">Create Booking</p>
          </a>
          <a href="/jobs" className="p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors">
            <ClipboardList className="w-8 h-8 text-primary-600 mb-2" />
            <p className="font-medium text-gray-900">View Jobs</p>
          </a>
        </div>
      </div>
    </div>
  );
}

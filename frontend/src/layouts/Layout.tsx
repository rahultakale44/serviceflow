import { useState } from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Car,
  Wrench,
  Calendar,
  ClipboardList,
  FileText,
  UserCog,
  Building2,
  CalendarCheck,
  Menu,
  X,
} from 'lucide-react';

const navItems = [
  { path: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { path: '/customers', icon: Users, label: 'Customers' },
  { path: '/vehicles', icon: Car, label: 'Vehicles' },
  { path: '/services', icon: Wrench, label: 'Services' },
  { path: '/bookings', icon: Calendar, label: 'Bookings' },
  { path: '/jobs', icon: ClipboardList, label: 'Job Cards' },
  { path: '/technicians', icon: UserCog, label: 'Technicians' },
  { path: '/service-bays', icon: Building2, label: 'Service Bays' },
  { path: '/assignments', icon: CalendarCheck, label: 'Assignments' },
];

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname.startsWith(path);
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:static inset-y-0 left-0 z-30
          w-64 bg-sidebar text-white
          transform transition-transform duration-300 ease-in-out
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        <div className="flex flex-col h-full">
          {/* Logo */}
          <div className="flex items-center justify-between h-16 px-6 border-b border-sidebar-light">
            <div className="flex items-center space-x-2">
              <Wrench className="w-8 h-8 text-primary-400" />
              <span className="text-xl font-bold">ServiceFlow</span>
            </div>
            <button
              className="lg:hidden text-gray-400 hover:text-white"
              onClick={() => setSidebarOpen(false)}
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = isActive(item.path);
              
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`
                    flex items-center space-x-3 px-4 py-3 rounded-lg
                    transition-colors duration-200
                    ${
                      active
                        ? 'bg-primary-600 text-white'
                        : 'text-gray-300 hover:bg-sidebar-light hover:text-white'
                    }
                  `}
                  onClick={() => setSidebarOpen(false)}
                >
                  <Icon className="w-5 h-5" />
                  <span className="font-medium">{item.label}</span>
                </Link>
              );
            })}
          </nav>

          {/* Footer */}
          <div className="p-4 border-t border-sidebar-light text-sm text-gray-400">
            <p>ServiceFlow v1.0</p>
            <p className="text-xs mt-1">© 2026 All rights reserved</p>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top bar */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center px-6">
          <button
            className="lg:hidden text-gray-600 hover:text-gray-900 mr-4"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="w-6 h-6" />
          </button>
          
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900">
              {navItems.find((item) => isActive(item.path))?.label || 'ServiceFlow'}
            </h1>
          </div>

          <div className="flex items-center space-x-4">
            <div className="text-sm text-gray-600">
              <p className="font-medium">Development Mode</p>
              <p className="text-xs text-gray-500">No authentication</p>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

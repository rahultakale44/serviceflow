import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './layouts/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import CustomerForm from './pages/CustomerForm';
import Vehicles from './pages/Vehicles';
import VehicleForm from './pages/VehicleForm';
import Services from './pages/Services';
import ServiceForm from './pages/ServiceForm';
import Bookings from './pages/Bookings';
import BookingForm from './pages/BookingForm';
import Jobs from './pages/Jobs';
import JobDetails from './pages/JobDetails';
import Estimates from './pages/Estimates';
import EstimateForm from './pages/EstimateForm';
import Technicians from './pages/Technicians';
import TechnicianForm from './pages/TechnicianForm';
import ServiceBays from './pages/ServiceBays';
import ServiceBayForm from './pages/ServiceBayForm';
import Assignments from './pages/Assignments';
import AssignmentForm from './pages/AssignmentForm';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />

          {/* Protected routes — all wrapped with PrivateRoute via Layout */}
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />

            <Route path="customers">
              <Route index element={<Customers />} />
              <Route path="new" element={<CustomerForm />} />
              <Route path=":id/edit" element={<CustomerForm />} />
            </Route>

            <Route path="vehicles">
              <Route index element={<Vehicles />} />
              <Route path="new" element={<VehicleForm />} />
              <Route path=":id/edit" element={<VehicleForm />} />
            </Route>

            <Route path="services">
              <Route index element={<Services />} />
              <Route path="new" element={<ServiceForm />} />
              <Route path=":id/edit" element={<ServiceForm />} />
            </Route>

            <Route path="bookings">
              <Route index element={<Bookings />} />
              <Route path="new" element={<BookingForm />} />
            </Route>

            <Route path="jobs">
              <Route index element={<Jobs />} />
              <Route path=":id" element={<JobDetails />} />
            </Route>

            <Route path="jobs/:jobId/estimates">
              <Route index element={<Estimates />} />
              <Route path="new" element={<EstimateForm />} />
              <Route path=":id/edit" element={<EstimateForm />} />
            </Route>

            <Route path="technicians">
              <Route index element={<Technicians />} />
              <Route path="new" element={<TechnicianForm />} />
              <Route path=":id/edit" element={<TechnicianForm />} />
            </Route>

            <Route path="service-bays">
              <Route index element={<ServiceBays />} />
              <Route path="new" element={<ServiceBayForm />} />
              <Route path=":id/edit" element={<ServiceBayForm />} />
            </Route>

            <Route path="assignments">
              <Route index element={<Assignments />} />
              <Route path="new" element={<AssignmentForm />} />
            </Route>
          </Route>

          {/* Fallback: redirect unknown paths to dashboard */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

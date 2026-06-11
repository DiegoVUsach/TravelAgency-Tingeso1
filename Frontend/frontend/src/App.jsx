import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthProvider';

import Navbar from './components/Navbar';
import LandingPage from './pages/LandingPage';
import Catalog from './pages/Catalog';
import BookingFlow from './pages/BookingFlow';
import Payment from './pages/Payment';
import MyReservations from './pages/MyReservations';
import MyProfile from './pages/MyProfile';
import AdminDashboard from './pages/AdminDashboard';
import AdminPackageForm from './pages/AdminPackageForm';
import AdminUsers from './pages/AdminUsers';
import PackageDetails from './pages/PackageDetails';
import AdminReports from './pages/AdminReports';
import AdminReservations from './pages/AdminReservations';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, hasRole } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/" />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/" />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <div className="main-content">
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/catalog" element={<Catalog />} />
            <Route path="/package/:id" element={<PackageDetails />} />
            
            {/* Client Routes */}
            <Route path="/book/:id" element={
              <ProtectedRoute requiredRole="USER">
                <BookingFlow />
              </ProtectedRoute>
            } />
            <Route path="/payment/:reservationId" element={
              <ProtectedRoute requiredRole="USER">
                <Payment />
              </ProtectedRoute>
            } />
            <Route path="/my-reservations" element={
              <ProtectedRoute requiredRole="USER">
                <MyReservations />
              </ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute requiredRole="USER">
                <MyProfile />
              </ProtectedRoute>
            } />

            {/* Admin Routes */}
            <Route path="/admin/dashboard" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            } />
            <Route path="/admin/reservations" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminReservations />
              </ProtectedRoute>
            } />
            <Route path="/admin/package/new" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminPackageForm />
              </ProtectedRoute>
            } />
            <Route path="/admin/package/edit/:id" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminPackageForm />
              </ProtectedRoute>
            } />
            <Route path="/admin/reports" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminReports />
              </ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminUsers />
              </ProtectedRoute>
            } />

            {/* Fallback Route */}
            <Route path="*" element={<h2>404 - Page Not Found</h2>} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

// We will create these components in the next steps
import Navbar from './components/Navbar';
import Home from './pages/Home';
// import Catalog from './pages/Catalog';
// import Booking from './pages/Booking';
// import Payment from './pages/Payment';
// import MyReservations from './pages/MyReservations';
// import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      {/* Navbar goes outside Routes so it shows on every page */}
      {/* <Navbar /> */}
      
      <div className="main-content">
        <Routes>
          {/* Public and Client Routes */}
          { <Route path="/" element={<Home />} /> }
          {/* <Route path="/catalog" element={<Catalog />} /> */} {/* Epic 3: Search */}
          {/* <Route path="/booking/:packageId" element={<Booking />} /> */} {/* Epic 4: Booking */}
          {/* <Route path="/payment/:reservationId" element={<Payment />} /> */} {/* Epic 5: Payment */}
          {/* <Route path="/my-reservations" element={<MyReservations />} /> */} {/* Epic 6: Client View */}

          {/* Admin / Agency Routes */}
          {/* <Route path="/admin/dashboard" element={<AdminDashboard />} /> */} {/* Epic 2 & 7: Admin & Reports */}

          {/* Fallback Route for 404 Not Found */}
          <Route path="*" element={<h2>404 - Page Not Found</h2>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
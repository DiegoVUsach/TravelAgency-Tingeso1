import React from 'react';
import { Navbar as BootstrapNavbar, Container, Nav, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthProvider';

function Navbar() {
  const { isAuthenticated, user, hasRole, login, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <BootstrapNavbar bg="dark" variant="dark" expand="lg" className="mb-4">
      <Container>
        <BootstrapNavbar.Brand as={Link} to="/">TravelAgency</BootstrapNavbar.Brand>
        <BootstrapNavbar.Toggle aria-controls="basic-navbar-nav" />
        <BootstrapNavbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">Catalog</Nav.Link>
            
            {isAuthenticated && hasRole('USER') && (
              <Nav.Link as={Link} to="/my-reservations">My Reservations</Nav.Link>
            )}
            
            {isAuthenticated && hasRole('ADMIN') && (
              <>
                <Nav.Link as={Link} to="/admin/dashboard">Admin Dashboard</Nav.Link>
                <Nav.Link as={Link} to="/admin/reports">Reports</Nav.Link>
              </>
            )}
          </Nav>
          
          <Nav className="ms-auto align-items-center">
            {isAuthenticated ? (
              <>
                <BootstrapNavbar.Text className="text-light me-3">
                  Signed in as: <strong>{user?.firstName || user?.username || 'User'}</strong>
                </BootstrapNavbar.Text>
                <Button variant="outline-light" size="sm" onClick={() => { logout(); navigate('/'); }}>
                  Logout
                </Button>
              </>
            ) : (
              <Button variant="primary" size="sm" onClick={login}>
                Login
              </Button>
            )}
          </Nav>
        </BootstrapNavbar.Collapse>
      </Container>
    </BootstrapNavbar>
  );
}

export default Navbar;
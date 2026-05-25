import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { useUser } from '../context/UserContext';

function AdminDashboard() {
  const [bundles, setBundles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { role } = useUser();

  useEffect(() => {
    fetchBundles();
  }, []);

  const fetchBundles = () => {
    setLoading(true);
    bundleService.getAllBundles()
      .then(data => {
        setBundles(data);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to fetch packages from the server.');
        setLoading(false);
      });
  };

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to deactivate/delete this package?')) {
      bundleService.deleteBundle(id)
        .then(() => {
          fetchBundles(); // refresh list
        })
        .catch(err => {
          alert('Failed to delete package. It might have active reservations.');
        });
    }
  };

  if (role !== 'ADMIN') {
    return (
      <Container className="my-5 text-center py-5 glass-panel">
        <h3 className="mb-4 text-danger">Access Denied</h3>
        <p className="text-muted mb-4">You do not have permission to view the Admin Dashboard.</p>
        <Alert variant="warning" className="d-inline-block">Please use the 'Mock Auth' dropdown in the Navbar to switch to 'Admin'.</Alert>
      </Container>
    );
  }

  return (
    <Container className="my-5 animate-fade-up">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>Admin Dashboard</h2>
          <p className="text-muted">Manage your travel packages and catalog.</p>
        </div>
        <Button className="btn-premium" onClick={() => navigate('/admin/package/new')}>
          + Create New Package
        </Button>
      </div>

      {loading && <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>}
      {error && <Alert variant="danger">{error}</Alert>}

      {!loading && !error && (
        <div className="glass-panel overflow-hidden">
          <Table responsive hover className="mb-0 align-middle">
            <thead className="bg-light">
              <tr>
                <th className="py-3 px-4 border-0">ID</th>
                <th className="py-3 px-4 border-0">Package Name</th>
                <th className="py-3 px-4 border-0">Destiny</th>
                <th className="py-3 px-4 border-0">Price (CLP)</th>
                <th className="py-3 px-4 border-0">Spots</th>
                <th className="py-3 px-4 border-0">Status</th>
                <th className="py-3 px-4 border-0 text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {bundles.map(bundle => (
                <tr key={bundle.idBundle}>
                  <td className="px-4 text-muted">#{bundle.idBundle}</td>
                  <td className="px-4 fw-bold">{bundle.nameBundle}</td>
                  <td className="px-4 text-muted">{bundle.destinyBundle}</td>
                  <td className="px-4">${bundle.priceBundle.toLocaleString()}</td>
                  <td className="px-4">{bundle.amountBundle}</td>
                  <td className="px-4">
                    <Badge bg={bundle.stateBundle === 'AVAILABLE' ? 'success' : 'secondary'}>
                      {bundle.stateBundle}
                    </Badge>
                  </td>
                  <td className="px-4 text-end">
                    <Button 
                      variant="outline-primary" 
                      size="sm" 
                      className="me-2"
                      onClick={() => navigate(`/admin/package/edit/${bundle.idBundle}`)}
                    >
                      Edit
                    </Button>
                    <Button 
                      variant="outline-danger" 
                      size="sm"
                      onClick={() => handleDelete(bundle.idBundle)}
                    >
                      Delete
                    </Button>
                  </td>
                </tr>
              ))}
              {bundles.length === 0 && (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">No packages found. Create one to get started.</td>
                </tr>
              )}
            </tbody>
          </Table>
        </div>
      )}
    </Container>
  );
}

export default AdminDashboard;

import React, { useState, useEffect } from 'react';
import { Container, Form, Button, Row, Col, Spinner, Alert } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { useUser } from '../context/UserContext';

function AdminPackageForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role } = useUser();
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    nameBundle: '',
    destinyBundle: '',
    descriptionBundle: '',
    priceBundle: '',
    amountBundle: '',
    departureBundle: '',
    arrivalBundle: '',
    durationBundle: '',
    stateBundle: 'AVAILABLE',
    experience: 'RELAX'
  });

  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEditMode) {
      bundleService.getBundleById(id)
        .then(data => {
          // Format dates to YYYY-MM-DD for input type="date"
          const formattedData = {
            ...data,
            departureBundle: data.departureBundle ? data.departureBundle.split('T')[0] : '',
            arrivalBundle: data.arrivalBundle ? data.arrivalBundle.split('T')[0] : ''
          };
          setFormData(formattedData);
          setLoading(false);
        })
        .catch(err => {
          setError('Failed to fetch package details.');
          setLoading(false);
        });
    }
  }, [id, isEditMode]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const validateForm = () => {
    if (Number(formData.priceBundle) <= 0) {
      setError("Price must be greater than zero.");
      return false;
    }
    if (Number(formData.amountBundle) <= 0) {
      setError("Total spots must be greater than zero.");
      return false;
    }
    if (new Date(formData.arrivalBundle) <= new Date(formData.departureBundle)) {
      setError("Arrival date must be after departure date.");
      return false;
    }
    if (formData.stateBundle === 'AVAILABLE' && Number(formData.amountBundle) === 0) {
      setError("Cannot publish as available if there are no spots.");
      return false;
    }
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSaving(true);
    setError(null);

    // Prepare payload
    const payload = {
      ...formData,
      priceBundle: Number(formData.priceBundle),
      amountBundle: Number(formData.amountBundle),
      durationBundle: Number(formData.durationBundle)
    };

    const request = isEditMode 
      ? bundleService.updateBundle(id, payload)
      : bundleService.createBundle(payload);

    request
      .then(() => {
        navigate('/admin/dashboard');
      })
      .catch(err => {
        setError('Failed to save package. Please try again.');
        setSaving(false);
      });
  };

  if (role !== 'ADMIN') {
    return (
      <Container className="my-5 text-center py-5 glass-panel">
        <h3 className="mb-4 text-danger">Access Denied</h3>
        <p className="text-muted">You do not have permission to view this page.</p>
      </Container>
    );
  }

  if (loading) return <Container className="text-center my-5 py-5"><Spinner animation="border" variant="primary" /></Container>;

  return (
    <Container className="my-5 max-w-md animate-fade-up" style={{ maxWidth: '900px' }}>
      <Button variant="link" className="text-muted ps-0 mb-4 text-decoration-none" onClick={() => navigate('/admin/dashboard')}>
        ← Back to Dashboard
      </Button>

      <div className="glass-panel p-5">
        <h2 className="mb-4">{isEditMode ? 'Edit Package' : 'Create New Package'}</h2>
        
        {error && <Alert variant="danger" className="mb-4">{error}</Alert>}

        <Form onSubmit={handleSubmit}>
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Package Name</Form.Label>
                <Form.Control required name="nameBundle" value={formData.nameBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Destiny</Form.Label>
                <Form.Control required name="destinyBundle" value={formData.destinyBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
          </Row>

          <Form.Group className="mb-3">
            <Form.Label className="fw-bold">Description</Form.Label>
            <Form.Control required as="textarea" rows={4} name="descriptionBundle" value={formData.descriptionBundle} onChange={handleInputChange} className="premium-input" />
          </Form.Group>

          <Row>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Price (CLP)</Form.Label>
                <Form.Control required type="number" min="1" name="priceBundle" value={formData.priceBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Total Spots</Form.Label>
                <Form.Control required type="number" min="1" name="amountBundle" value={formData.amountBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Duration (Days)</Form.Label>
                <Form.Control required type="number" min="1" name="durationBundle" value={formData.durationBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
          </Row>

          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Departure Date</Form.Label>
                <Form.Control required type="date" name="departureBundle" value={formData.departureBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Arrival Date</Form.Label>
                <Form.Control required type="date" name="arrivalBundle" value={formData.arrivalBundle} onChange={handleInputChange} className="premium-input" />
              </Form.Group>
            </Col>
          </Row>

          <Row className="mb-4">
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Experience Type</Form.Label>
                <Form.Select required name="experience" value={formData.experience || 'RELAX'} onChange={handleInputChange} className="premium-input">
                  <option value="RELAX">Relax</option>
                  <option value="ADVENTURE">Adventure</option>
                  <option value="CULTURAL">Cultural</option>
                  <option value="FAMILY">Family</option>
                  <option value="ROMANTIC">Romantic</option>
                  <option value="BUSINESS">Business</option>
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold">Status</Form.Label>
                <Form.Select required name="stateBundle" value={formData.stateBundle} onChange={handleInputChange} className="premium-input">
                  <option value="AVAILABLE">Available</option>
                  <option value="SOLD_OUT">Sold Out</option>
                  <option value="NOT_AVAILABLE">Not Available / Draft</option>
                  <option value="CANCELLED">Cancelled</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>

          <div className="d-flex justify-content-end border-top pt-4">
            <Button variant="outline-secondary" className="me-3 px-4" onClick={() => navigate('/admin/dashboard')} disabled={saving}>
              Cancel
            </Button>
            <Button type="submit" className="btn-premium px-5" disabled={saving}>
              {saving ? <Spinner size="sm" /> : 'Save Package'}
            </Button>
          </div>
        </Form>
      </div>
    </Container>
  );
}

export default AdminPackageForm;

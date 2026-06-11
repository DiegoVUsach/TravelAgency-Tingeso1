import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Button, Badge, Spinner, Alert } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';

function PackageDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [bundle, setBundle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    bundleService.getBundleById(id)
      .then(data => {
        setBundle(data);
        setLoading(false);
      })
      .catch(err => {
        setError('Could not fetch package details. Please try again.');
        setLoading(false);
      });
  }, [id]);

  const getImageUrl = (type) => {
    const images = {
      RELAX: 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?q=80&w=2070&auto=format&fit=crop',
      ADVENTURE: 'https://images.unsplash.com/photo-1533240332313-0db49b459ad6?q=80&w=1974&auto=format&fit=crop',
      CULTURAL: 'https://images.unsplash.com/photo-1518391846015-55a9cc003b25?q=80&w=2070&auto=format&fit=crop',
      FAMILY: 'https://images.unsplash.com/photo-1511895426328-dc8714191300?q=80&w=2070&auto=format&fit=crop',
      ROMANTIC: 'https://images.unsplash.com/photo-1516815231560-8f41ec531527?q=80&w=2067&auto=format&fit=crop',
      BUSINESS: 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=2069&auto=format&fit=crop'
    };
    return images[type] || 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=1935&auto=format&fit=crop';
  };

  if (loading) {
    return (
      <Container className="text-center my-5 py-5">
        <Spinner animation="grow" variant="primary" />
        <p className="mt-3 text-muted">Loading package details...</p>
      </Container>
    );
  }

  if (error || !bundle) {
    return (
      <Container className="my-5">
        <Alert variant="danger" className="text-center rounded-4 shadow-sm">
          {error || "Package not found"}
        </Alert>
        <div className="text-center mt-4">
          <Button variant="outline-primary" onClick={() => navigate('/')}>Back to Catalog</Button>
        </div>
      </Container>
    );
  }

  const isAvailable = bundle.stateBundle === 'AVAILABLE';

  return (
    <Container className="my-5 animate-fade-up">
      <Row className="mb-4">
        <Col>
          <Button variant="link" className="text-decoration-none ps-0" style={{ color: 'var(--text-muted)' }} onClick={() => navigate(-1)}>
            ← Back
          </Button>
        </Col>
      </Row>

      <Row>
        {/* Image Section */}
        <Col lg={7} className="mb-4">
          <div style={{ borderRadius: 'var(--border-radius-lg)', overflow: 'hidden', boxShadow: 'var(--shadow-md)', position: 'relative' }}>
            <div className="premium-badge" style={{ fontSize: '1rem', padding: '10px 20px' }}>
              {bundle.tipoExperienciaBundle || 'PREMIUM'}
            </div>
            <img 
              src={getImageUrl(bundle.tipoExperienciaBundle)} 
              alt={bundle.nameBundle} 
              style={{ width: '100%', height: '500px', objectFit: 'cover' }}
            />
          </div>
        </Col>

        {/* Details Section */}
        <Col lg={5}>
          <div className="glass-panel p-5 h-100 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <Badge bg={isAvailable ? 'success' : 'danger'} style={{ fontSize: '0.9rem', padding: '8px 16px', borderRadius: '20px' }}>
                {bundle.stateBundle}
              </Badge>
              <span className="text-muted fw-bold">⏳ {bundle.durationBundle} Days</span>
            </div>

            <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>{bundle.nameBundle}</h1>
            <p className="text-muted mb-4 d-flex align-items-center fs-5">
              <span style={{ color: 'var(--secondary-color)', marginRight: '8px' }}>📍</span> 
              {bundle.destinyBundle}
            </p>

            <div className="mb-4 flex-grow-1">
              <h5 className="mb-3">About this experience</h5>
              <p style={{ lineHeight: '1.8', color: 'var(--text-muted)' }}>
                {bundle.descBundle}
              </p>
            </div>

            <div className="border-top pt-4 mb-4">
              <Row>
                <Col xs={6}>
                  <p className="text-muted small mb-1">Available Spots</p>
                  <p className="fw-bold fs-5">{bundle.availableSlotsBundle}</p>
                </Col>
                <Col xs={6}>
                  <p className="text-muted small mb-1">Dates</p>
                  <p className="fw-bold">
                    {new Date(bundle.startDateBundle).toLocaleDateString()} - {new Date(bundle.endDateBundle).toLocaleDateString()}
                  </p>
                </Col>
              </Row>
            </div>

            <div className="mt-auto bg-light p-4 rounded-4 text-center">
              <p className="text-muted mb-1 text-uppercase small fw-bold">Total Price</p>
              <h2 style={{ color: 'var(--primary-color)', fontSize: '2.5rem', marginBottom: '1.5rem' }}>
                ${bundle.priceBundle.toLocaleString()} <span className="fs-6 text-muted">CLP / person</span>
              </h2>
              
              <Button 
                className={`w-100 ${isAvailable ? 'btn-premium' : 'btn-secondary'}`} 
                style={{ padding: '16px', fontSize: '1.1rem' }}
                disabled={!isAvailable}
                onClick={() => navigate(`/book/${bundle.idBundle}`)}
              >
                {isAvailable ? 'Book This Experience' : 'Currently Unavailable'}
              </Button>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default PackageDetails;

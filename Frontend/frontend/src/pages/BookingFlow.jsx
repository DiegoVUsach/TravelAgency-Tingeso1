import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Spinner, Alert, ProgressBar } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { reservationService } from '../services/reservationService';
import { useAuth } from '../context/AuthProvider';

function BookingFlow() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  
  const [bundle, setBundle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [step, setStep] = useState(1);
  const [passengers, setPassengers] = useState(1);
  const [specialRequests, setSpecialRequests] = useState('');
  
  const [quoteData, setQuoteData] = useState(null);
  const [loadingQuote, setLoadingQuote] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    bundleService.getBundleById(id)
      .then(data => {
        setBundle(data);
        setLoading(false);
      })
      .catch(err => {
        setError('Could not fetch package details.');
        setLoading(false);
      });
  }, [id]);

  useEffect(() => {
    if (bundle && isAuthenticated) {
      updateQuote(passengers);
    }
  }, [bundle, passengers, isAuthenticated]);

  const updateQuote = (passengerCount) => {
    setLoadingQuote(true);
    const reservationData = {
      items: [{ bundleId: bundle.idBundle, passengers: passengerCount }]
    };

    reservationService.quoteReservation(reservationData)
      .then((data) => {
        setQuoteData(data);
        setLoadingQuote(false);
      })
      .catch(err => {
        console.error("Failed to fetch quote", err);
        setLoadingQuote(false);
      });
  };

  if (!isAuthenticated) {
    return (
      <Container className="my-5 text-center py-5 glass-panel">
        <h3 className="mb-4 text-primary">Authentication Required</h3>
        <p className="text-muted mb-4">You must be logged in to make a reservation.</p>
        <Button variant="primary" onClick={() => navigate('/')}>Back to Catalog</Button>
      </Container>
    );
  }

  if (loading) return <Container className="text-center my-5 py-5"><Spinner animation="border" variant="primary" /></Container>;
  if (error || !bundle) return <Container className="my-5"><Alert variant="danger">{error || "Package not found"}</Alert></Container>;

  const handleNextStep = () => {
    if (step === 1 && (passengers < 1 || passengers > bundle.availableSlotsBundle)) {
      alert(`Please select between 1 and ${bundle.availableSlotsBundle} passengers.`);
      return;
    }
    setStep(step + 1);
  };

  const handleConfirmReservation = () => {
    setIsSubmitting(true);
    
    const reservationData = {
      items: [
        {
          bundleId: bundle.idBundle,
          passengers: passengers
        }
      ]
    };

    reservationService.createReservation(reservationData)
      .then((data) => {
        setIsSubmitting(false);
        if (data && data.generatedReservationIds && data.generatedReservationIds.length > 0) {
          navigate(`/payment/${data.generatedReservationIds[0]}`, { state: { amount: quoteData?.finalTotal || 0, bundleName: bundle.nameBundle } });
        } else {
          setSuccess(true);
        }
      })
      .catch(err => {
        console.error(err);
        alert(err.response?.data?.message || 'Failed to create reservation. Please try again.');
        setIsSubmitting(false);
      });
  };

  if (success) {
    return (
      <Container className="my-5 text-center animate-fade-up">
        <div className="glass-panel p-5 d-inline-block">
          <div className="mb-4" style={{ fontSize: '4rem' }}>✅</div>
          <h2 className="text-success mb-3">Reservation Confirmed!</h2>
          <p className="text-muted mb-4">Your reservation for <strong>{bundle.nameBundle}</strong> has been successfully created.</p>
          <Button className="btn-premium" onClick={() => navigate('/my-reservations')}>
            View My Reservations
          </Button>
        </div>
      </Container>
    );
  }

  return (
    <Container className="my-5 max-w-md animate-fade-up" style={{ maxWidth: '800px' }}>
      <Button variant="link" className="text-muted ps-0 mb-4 text-decoration-none" onClick={() => navigate(-1)}>
        ← Back to Details
      </Button>

      <div className="mb-5">
        <h2 className="mb-3">Booking: {bundle.nameBundle}</h2>
        <ProgressBar now={(step / 2) * 100} className="mb-2" style={{ height: '8px' }} />
        <div className="d-flex justify-content-between text-muted small fw-bold text-uppercase">
          <span>Step 1: Details</span>
          <span>Step 2: Summary</span>
        </div>
      </div>

      <Row>
        <Col md={8}>
          <div className="glass-panel p-4 mb-4">
            {step === 1 && (
              <div className="animate-fade-up">
                <h4 className="mb-4">Traveler Information</h4>
                
                <Form.Group className="mb-4">
                  <Form.Label className="fw-bold">Number of Passengers</Form.Label>
                  <div className="d-flex align-items-center gap-3">
                    <Button variant="outline-primary" className="rounded-circle" style={{ width: '40px', height: '40px' }}
                      onClick={() => setPassengers(Math.max(1, passengers - 1))} disabled={passengers <= 1}>-</Button>
                    <span className="fs-4 fw-bold">{passengers}</span>
                    <Button variant="outline-primary" className="rounded-circle" style={{ width: '40px', height: '40px' }}
                      onClick={() => setPassengers(Math.min(bundle.availableSlotsBundle, passengers + 1))} disabled={passengers >= bundle.availableSlotsBundle}>+</Button>
                  </div>
                  <Form.Text className="text-muted">
                    Available spots: {bundle.availableSlotsBundle}
                  </Form.Text>
                </Form.Group>

                <Form.Group className="mb-4">
                  <Form.Label className="fw-bold">Special Requests (Optional)</Form.Label>
                  <Form.Control 
                    as="textarea" 
                    rows={3} 
                    className="premium-input"
                    value={specialRequests}
                    onChange={(e) => setSpecialRequests(e.target.value)}
                    placeholder="E.g., dietary restrictions, accessibility needs..."
                  />
                </Form.Group>

                <Button className="btn-premium w-100" onClick={handleNextStep} disabled={loadingQuote}>
                  {loadingQuote ? <Spinner size="sm" /> : 'Continue to Summary'}
                </Button>
              </div>
            )}

            {step === 2 && (
              <div className="animate-fade-up">
                <h4 className="mb-4">Review Your Booking</h4>
                <Alert variant="info" className="border-0 bg-light">
                  <h6 className="mb-2 text-uppercase fw-bold text-primary">Important Information</h6>
                  <ul className="mb-0 small text-muted">
                    <li>By confirming, you agree to the terms and conditions.</li>
                    <li>This reservation will be marked as "Pending Payment".</li>
                    <li>Please complete payment within 48 hours to secure your spots.</li>
                  </ul>
                </Alert>

                <div className="mt-4">
                  <Button className="btn-secondary me-3" onClick={() => setStep(1)} disabled={isSubmitting}>
                    Go Back
                  </Button>
                  <Button className="btn-premium" onClick={handleConfirmReservation} disabled={isSubmitting || loadingQuote}>
                    {isSubmitting ? <><Spinner size="sm" className="me-2"/> Processing...</> : 'Confirm Reservation'}
                  </Button>
                </div>
              </div>
            )}
          </div>
        </Col>

        <Col md={4}>
          <div className="filter-sidebar p-4 border border-light">
            <h5 className="mb-4 border-bottom pb-3">Order Summary</h5>
            
            <div className="mb-3">
              <span className="text-muted small d-block">Package</span>
              <span className="fw-bold">{bundle.nameBundle}</span>
            </div>
            
            <div className="mb-3 border-bottom pb-3">
              <span className="text-muted small d-block">Passengers</span>
              <span className="fw-bold">{passengers} Person(s)</span>
            </div>

            {loadingQuote ? (
              <div className="text-center py-4"><Spinner animation="border" variant="primary" size="sm" /></div>
            ) : (
              <>
                <div className="d-flex justify-content-between mb-2 text-muted">
                  <span>Base Price ({passengers}x)</span>
                  <span>${quoteData?.subtotal?.toLocaleString() || (bundle.priceBundle * passengers).toLocaleString()}</span>
                </div>

                {quoteData && quoteData.totalDiscount > 0 && (
                  <div className="d-flex justify-content-between mb-3 text-success small border-bottom pb-3">
                    <span>Applied Discounts</span>
                    <span>-${quoteData.totalDiscount.toLocaleString()}</span>
                  </div>
                )}

                <div className="d-flex justify-content-between mt-3 pt-2">
                  <span className="fw-bold text-uppercase">Total Due</span>
                  <span className="fw-bold fs-4 text-primary">${quoteData?.finalTotal?.toLocaleString() || (bundle.priceBundle * passengers).toLocaleString()}</span>
                </div>
              </>
            )}
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default BookingFlow;

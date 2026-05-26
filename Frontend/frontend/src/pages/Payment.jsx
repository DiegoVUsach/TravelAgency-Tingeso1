import React, { useState } from 'react';
import { Container, Row, Col, Form, Button, Card, Spinner, Alert } from 'react-bootstrap';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { paymentService } from '../services/paymentService';

function Payment() {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  
  // The amount and bundleName passed from BookingFlow via state
  const amount = location.state?.amount || 0;
  const bundleName = location.state?.bundleName || "Unknown Package";

  const [formData, setFormData] = useState({
    cardNumber: '',
    cardName: '',
    expiry: '',
    cvv: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handlePayment = (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    // Mock validation
    if (!formData.cardNumber || !formData.cardName || !formData.expiry || !formData.cvv) {
      setError("Please fill all the credit card fields.");
      setIsSubmitting(false);
      return;
    }

    const payload = {
      reservationId: parseInt(reservationId),
      amount: amount,
      paymentMethod: 'CREDIT_CARD'
    };

    paymentService.processPayment(payload)
      .then(() => {
        setIsSubmitting(false);
        setSuccess(true);
      })
      .catch(err => {
        setIsSubmitting(false);
        setError("Failed to process payment. Please try again.");
      });
  };

  if (!amount) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="warning">Invalid payment session. Please start your booking again.</Alert>
        <Button variant="primary" onClick={() => navigate('/')}>Back to Catalog</Button>
      </Container>
    );
  }

  if (success) {
    return (
      <Container className="my-5 text-center animate-fade-up">
        <div className="glass-panel p-5 d-inline-block">
          <div className="mb-4" style={{ fontSize: '4rem' }}>🎉</div>
          <h2 className="text-success mb-3">Payment Successful!</h2>
          <p className="text-muted mb-4">Your payment of <strong>${amount.toLocaleString()} CLP</strong> has been received.</p>
          <p className="mb-4">Your reservation for <strong>{bundleName}</strong> is now CONFIRMED.</p>
          <Button className="btn-premium" onClick={() => navigate('/my-reservations')}>
            View My Reservations
          </Button>
        </div>
      </Container>
    );
  }

  return (
    <Container className="my-5 max-w-md animate-fade-up" style={{ maxWidth: '900px' }}>
      <h2 className="mb-4">Secure Checkout</h2>
      <Row>
        <Col md={7}>
          <div className="glass-panel p-4 mb-4">
            <h4 className="mb-4">Credit Card Details</h4>
            <Alert variant="info" className="small">
              Note: This is a simulated payment gateway. Do not enter real credit card information. Data is not saved.
            </Alert>
            <Form onSubmit={handlePayment}>
              <Form.Group className="mb-3">
                <Form.Label>Cardholder Name</Form.Label>
                <Form.Control 
                  type="text" 
                  name="cardName"
                  placeholder="John Doe" 
                  value={formData.cardName}
                  onChange={handleInputChange}
                  required 
                />
              </Form.Group>
              
              <Form.Group className="mb-3">
                <Form.Label>Card Number</Form.Label>
                <Form.Control 
                  type="text" 
                  name="cardNumber"
                  placeholder="0000 0000 0000 0000" 
                  maxLength={19}
                  value={formData.cardNumber}
                  onChange={handleInputChange}
                  required 
                />
              </Form.Group>

              <Row>
                <Col xs={6}>
                  <Form.Group className="mb-3">
                    <Form.Label>Expiry Date</Form.Label>
                    <Form.Control 
                      type="text" 
                      name="expiry"
                      placeholder="MM/YY" 
                      maxLength={5}
                      value={formData.expiry}
                      onChange={handleInputChange}
                      required 
                    />
                  </Form.Group>
                </Col>
                <Col xs={6}>
                  <Form.Group className="mb-4">
                    <Form.Label>CVV</Form.Label>
                    <Form.Control 
                      type="text" 
                      name="cvv"
                      placeholder="123" 
                      maxLength={4}
                      value={formData.cvv}
                      onChange={handleInputChange}
                      required 
                    />
                  </Form.Group>
                </Col>
              </Row>

              {error && <Alert variant="danger">{error}</Alert>}

              <Button className="btn-premium w-100 py-3 mt-2" type="submit" disabled={isSubmitting}>
                {isSubmitting ? <Spinner size="sm" className="me-2"/> : `Confirm Payment of $${amount.toLocaleString()} CLP`}
              </Button>
            </Form>
          </div>
        </Col>

        <Col md={5}>
          <div className="filter-sidebar p-4 border border-light">
            <h5 className="mb-4 border-bottom pb-3">Payment Summary</h5>
            
            <div className="mb-3">
              <span className="text-muted small d-block">Reservation ID</span>
              <span className="fw-bold">#{reservationId}</span>
            </div>
            
            <div className="mb-3">
              <span className="text-muted small d-block">Package</span>
              <span className="fw-bold">{bundleName}</span>
            </div>
            
            <div className="d-flex justify-content-between mt-4 pt-3 border-top">
              <span className="fw-bold text-uppercase">Total Amount</span>
              <span className="fw-bold fs-4 text-primary">${amount.toLocaleString()}</span>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default Payment;

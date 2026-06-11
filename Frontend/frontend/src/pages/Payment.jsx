import React, { useState } from 'react';
import {
  Container, Box, Typography, TextField, Button, Paper, Grid,
  CircularProgress, Alert
} from '@mui/material';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import LockIcon from '@mui/icons-material/Lock';
import CelebrationIcon from '@mui/icons-material/Celebration';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { paymentService } from '../services/paymentService';

function Payment() {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const amount = location.state?.amount || 0;
  const bundleName = location.state?.bundleName || 'Unknown Package';

  const [formData, setFormData] = useState({ cardNumber: '', cardName: '', expiry: '', cvv: '' });
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

    if (!formData.cardNumber || !formData.cardName || !formData.expiry || !formData.cvv) {
      setError('Please fill all the credit card fields.');
      setIsSubmitting(false);
      return;
    }

    paymentService.processPayment({
      reservationId: parseInt(reservationId),
      amount,
      paymentMethod: 'CREDIT_CARD',
      cardNumber: formData.cardNumber,
      expirationDate: formData.expiry,
      cvv: formData.cvv,
    })
      .then(() => { setIsSubmitting(false); setSuccess(true); })
      .catch(() => { setIsSubmitting(false); setError('Failed to process payment. Please try again.'); });
  };

  if (!amount) {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
        <Alert severity="warning" sx={{ mb: 3 }}>Invalid payment session. Please start your booking again.</Alert>
        <Button variant="contained" onClick={() => navigate('/catalog')}>Back to Catalog</Button>
      </Container>
    );
  }

  if (success) {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }} className="animate-fade-up">
        <Paper sx={{ p: 5, borderRadius: 3 }}>
          <CelebrationIcon sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
          <Typography variant="h4" color="success.main" sx={{ mb: 1 }}>Payment Successful!</Typography>
          <Typography color="text.secondary" sx={{ mb: 1 }}>
            Your payment of <strong>${amount.toLocaleString()} CLP</strong> has been received.
          </Typography>
          <Typography sx={{ mb: 3 }}>
            Your reservation for <strong>{bundleName}</strong> is now <strong>CONFIRMED</strong>.
          </Typography>
          <Button variant="contained" onClick={() => navigate('/my-reservations')}>View My Reservations</Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }} className="animate-fade-up">
      <Typography variant="h4" sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 1 }}>
        <LockIcon color="primary" /> Secure Checkout
      </Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 7 }}>
          <Paper sx={{ p: 4, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
              <CreditCardIcon color="primary" /> Credit Card Details
            </Typography>
            <Alert severity="info" sx={{ mb: 3, fontSize: '0.8rem' }}>
              This is a simulated payment gateway. Do not enter real credit card information.
            </Alert>

            <Box component="form" onSubmit={handlePayment}>
              <TextField fullWidth label="Cardholder Name" name="cardName" value={formData.cardName}
                onChange={handleInputChange} required sx={{ mb: 2 }} placeholder="John Doe" />
              <TextField fullWidth label="Card Number" name="cardNumber" value={formData.cardNumber}
                onChange={handleInputChange} required sx={{ mb: 2 }} placeholder="0000 0000 0000 0000"
                inputProps={{ maxLength: 19 }} />
              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid size={6}>
                  <TextField fullWidth label="Expiry Date" name="expiry" value={formData.expiry}
                    onChange={handleInputChange} required placeholder="MM/YY" inputProps={{ maxLength: 5 }} />
                </Grid>
                <Grid size={6}>
                  <TextField fullWidth label="CVV" name="cvv" value={formData.cvv}
                    onChange={handleInputChange} required placeholder="123" inputProps={{ maxLength: 4 }} />
                </Grid>
              </Grid>

              {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

              <Button fullWidth variant="contained" type="submit" disabled={isSubmitting} size="large" sx={{ py: 1.5 }}>
                {isSubmitting ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
                {isSubmitting ? 'Processing...' : `Confirm Payment of $${amount.toLocaleString()} CLP`}
              </Button>
            </Box>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ mb: 3, pb: 2, borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
              Payment Summary
            </Typography>
            <Box sx={{ mb: 2 }}>
              <Typography variant="caption" color="text.secondary">Reservation ID</Typography>
              <Typography fontWeight={600}>#{reservationId}</Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="caption" color="text.secondary">Package</Typography>
              <Typography fontWeight={600}>{bundleName}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', pt: 3, mt: 2, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
              <Typography fontWeight={700} textTransform="uppercase">Total Amount</Typography>
              <Typography variant="h5" color="primary" fontWeight={800}>${amount.toLocaleString()}</Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}

export default Payment;

import React, { useState, useEffect } from 'react';
import {
  Container, Box, Typography, TextField, Button, Paper, Grid,
  CircularProgress, Alert, Stepper, Step, StepLabel, IconButton
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlined';
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
  const [step, setStep] = useState(0);
  const [passengers, setPassengers] = useState(1);
  const [specialRequests, setSpecialRequests] = useState('');
  const [quoteData, setQuoteData] = useState(null);
  const [loadingQuote, setLoadingQuote] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    bundleService.getBundleById(id)
      .then(data => { setBundle(data); setLoading(false); })
      .catch(() => { setError('No se pudieron obtener los detalles del paquete.'); setLoading(false); });
  }, [id]);

  useEffect(() => {
    if (bundle && isAuthenticated) updateQuote(passengers);
  }, [bundle, passengers, isAuthenticated]);

  const updateQuote = (count) => {
    setLoadingQuote(true);
    reservationService.quoteReservation({ items: [{ bundleId: bundle.idBundle, passengers: count }] })
      .then(data => { setQuoteData(data); setLoadingQuote(false); })
      .catch(() => setLoadingQuote(false));
  };

  if (!isAuthenticated) {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
        <Paper sx={{ p: 5, borderRadius: 3 }}>
          <Typography variant="h5" color="primary" sx={{ mb: 2 }}>Autenticación Requerida</Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>Debes iniciar sesión para hacer una reserva.</Typography>
          <Button variant="contained" onClick={() => navigate('/catalog')}>Volver al Catálogo</Button>
        </Paper>
      </Container>
    );
  }

  if (loading) return <Box sx={{ textAlign: 'center', py: 10 }}><CircularProgress /></Box>;
  if (error || !bundle) return <Container sx={{ py: 5 }}><Alert severity="error">{error || 'Paquete no encontrado'}</Alert></Container>;

  const handleNextStep = () => {
    if (step === 0 && (passengers < 1 || passengers > bundle.availableSlotsBundle)) {
      alert(`Por favor, selecciona entre 1 y ${bundle.availableSlotsBundle} pasajeros.`);
      return;
    }
    setStep(1);
  };

  const handleConfirmReservation = () => {
    setIsSubmitting(true);
    reservationService.createReservation({ items: [{ bundleId: bundle.idBundle, passengers }] })
      .then((data) => {
        setIsSubmitting(false);
        if (data?.generatedReservationIds?.length > 0) {
          navigate(`/payment/${data.generatedReservationIds[0]}`, {
            state: { amount: quoteData?.finalTotal || 0, bundleName: bundle.nameBundle }
          });
        } else {
          setSuccess(true);
        }
      })
      .catch(err => {
        alert(err.response?.data?.message || 'No se pudo crear la reserva.');
        setIsSubmitting(false);
      });
  };

  if (success) {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }} className="animate-fade-up">
        <Paper sx={{ p: 5, borderRadius: 3 }}>
          <CheckCircleOutlineIcon sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
          <Typography variant="h4" sx={{ mb: 1 }}>¡Reserva Confirmada!</Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            Tu reserva para <strong>{bundle.nameBundle}</strong> ha sido creada.
          </Typography>
          <Button variant="contained" onClick={() => navigate('/my-reservations')}>Ver Mis Reservas</Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }} className="animate-fade-up">
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)} sx={{ mb: 3, color: 'text.secondary' }}>
        Volver a los Detalles
      </Button>

      <Typography variant="h4" sx={{ mb: 1 }}>Reserva: {bundle.nameBundle}</Typography>

      <Stepper activeStep={step} sx={{ mb: 4 }}>
        <Step><StepLabel>Detalles del Viajero</StepLabel></Step>
        <Step><StepLabel>Revisar y Confirmar</StepLabel></Step>
      </Stepper>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Paper sx={{ p: 4, borderRadius: 3 }}>
            {step === 0 && (
              <Box className="animate-fade-up">
                <Typography variant="h6" sx={{ mb: 3 }}>Información del Viajero</Typography>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>Número de Pasajeros</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
                  <IconButton color="primary" onClick={() => setPassengers(Math.max(1, passengers - 1))} disabled={passengers <= 1}>
                    <RemoveIcon />
                  </IconButton>
                  <Typography variant="h4" fontWeight={700}>{passengers}</Typography>
                  <IconButton color="primary" onClick={() => setPassengers(Math.min(bundle.availableSlotsBundle, passengers + 1))} disabled={passengers >= bundle.availableSlotsBundle}>
                    <AddIcon />
                  </IconButton>
                </Box>
                <Typography variant="caption" color="text.secondary" sx={{ mb: 3, display: 'block' }}>
                  Cupos disponibles: {bundle.availableSlotsBundle}
                </Typography>
                <TextField
                  fullWidth multiline rows={3} label="Solicitudes Especiales (Opcional)"
                  value={specialRequests} onChange={(e) => setSpecialRequests(e.target.value)}
                  placeholder="Ej., restricciones dietéticas, necesidades de accesibilidad..."
                  sx={{ mb: 3 }}
                />
                <Button fullWidth variant="contained" onClick={handleNextStep} disabled={loadingQuote} size="large">
                  {loadingQuote ? <CircularProgress size={20} /> : 'Continuar al Resumen'}
                </Button>
              </Box>
            )}

            {step === 1 && (
              <Box className="animate-fade-up">
                <Typography variant="h6" sx={{ mb: 3 }}>Revisar Tu Reserva</Typography>
                <Alert severity="info" sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>Información Importante</Typography>
                  <ul style={{ margin: 0, paddingLeft: 20 }}>
                    <li>Al confirmar, aceptas los términos y condiciones.</li>
                    <li>Esta reserva será marcada como "Pendiente de Pago".</li>
                    <li>Por favor completa el pago dentro de las 48 horas.</li>
                  </ul>
                </Alert>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button variant="outlined" onClick={() => setStep(0)} disabled={isSubmitting}>Volver</Button>
                  <Button variant="contained" onClick={handleConfirmReservation} disabled={isSubmitting || loadingQuote}>
                    {isSubmitting ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
                    {isSubmitting ? 'Procesando...' : 'Confirmar Reserva'}
                  </Button>
                </Box>
              </Box>
            )}
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Paper sx={{ p: 3, borderRadius: 3, position: 'sticky', top: 80 }}>
            <Typography variant="h6" sx={{ mb: 2, pb: 2, borderBottom: '1px solid rgba(255,255,255,0.06)' }}>Resumen de la Orden</Typography>
            <Box sx={{ mb: 2 }}>
              <Typography variant="caption" color="text.secondary">Paquete</Typography>
              <Typography fontWeight={600}>{bundle.nameBundle}</Typography>
            </Box>
            <Box sx={{ mb: 2, pb: 2, borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
              <Typography variant="caption" color="text.secondary">Pasajeros</Typography>
              <Typography fontWeight={600}>{passengers} Persona(s)</Typography>
            </Box>
            {loadingQuote ? (
              <Box sx={{ textAlign: 'center', py: 3 }}><CircularProgress size={24} /></Box>
            ) : (
              <>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">Precio Base ({passengers}x)</Typography>
                  <Typography variant="body2">${quoteData?.subtotal?.toLocaleString() || (bundle.priceBundle * passengers).toLocaleString()}</Typography>
                </Box>
                {quoteData?.appliedDiscounts?.length > 0 && quoteData.appliedDiscounts.map((discount, idx) => {
                  const labelMap = {
                    VOLUME_DISCOUNT: '👥 Descuento grupal',
                    FREQUENT_CLIENT: '⭐ Cliente frecuente',
                    MULTIPLE_PACKAGES: '📦 Multi-paquete',
                    PROMOTION: '🏷️ Promoción',
                  };
                  return (
                    <Box key={idx} sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2" color="success.main">
                        {labelMap[discount.type] || discount.type} ({(discount.percentage * 100).toFixed(0)}%)
                      </Typography>
                      <Typography variant="body2" color="success.main">
                        -${discount.amount?.toLocaleString()}
                      </Typography>
                    </Box>
                  );
                })}
                {quoteData?.totalDiscount > 0 && !quoteData?.appliedDiscounts?.length && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="success.main">Descuentos</Typography>
                    <Typography variant="body2" color="success.main">-${quoteData.totalDiscount.toLocaleString()}</Typography>
                  </Box>
                )}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', pt: 2, mt: 2, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
                  <Typography fontWeight={700}>TOTAL</Typography>
                  <Typography variant="h5" color="primary" fontWeight={800}>
                    ${quoteData?.finalTotal?.toLocaleString() || (bundle.priceBundle * passengers).toLocaleString()}
                  </Typography>
                </Box>
              </>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}

export default BookingFlow;

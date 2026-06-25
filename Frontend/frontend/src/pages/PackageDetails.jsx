import React, { useState, useEffect } from 'react';
import {
  Container, Box, Typography, Grid, Button, Chip, CircularProgress, Alert, Paper
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import GroupIcon from '@mui/icons-material/Group';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import BlockIcon from '@mui/icons-material/Block';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';

const experienceLabelMap = {
  RELAX: 'Relajación', ADVENTURE: 'Aventura', CULTURAL: 'Cultural',
  FAMILY: 'Familiar', ROMANTIC: 'Romántico', BUSINESS: 'Negocios',
};

function PackageDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [bundle, setBundle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    bundleService.getBundleById(id)
      .then(data => { setBundle(data); setLoading(false); })
      .catch(() => { setError('No se pudo obtener los detalles del paquete.'); setLoading(false); });
  }, [id]);

  const getImageUrl = (types) => {
    const images = {
      RELAX: 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?q=80&w=1200&auto=format&fit=crop',
      ADVENTURE: 'https://images.unsplash.com/photo-1533240332313-0db49b459ad6?q=80&w=1200&auto=format&fit=crop',
      CULTURAL: 'https://images.unsplash.com/photo-1518391846015-55a9cc003b25?q=80&w=1200&auto=format&fit=crop',
      FAMILY: 'https://images.unsplash.com/photo-1511895426328-dc8714191300?q=80&w=1200&auto=format&fit=crop',
      ROMANTIC: 'https://images.unsplash.com/photo-1516815231560-8f41ec531527?q=80&w=1200&auto=format&fit=crop',
      BUSINESS: 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=1200&auto=format&fit=crop',
    };
    // Use the first experience type for the image
    const firstType = Array.isArray(types) && types.length > 0 ? types[0] : types;
    return images[firstType] || 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=1200&auto=format&fit=crop';
  };

  if (loading) return <Box sx={{ textAlign: 'center', py: 10 }}><CircularProgress /></Box>;
  if (error || !bundle) return <Container sx={{ py: 5 }}><Alert severity="error">{error || 'Paquete no encontrado'}</Alert></Container>;

  const isAvailable = bundle.stateBundle === 'AVAILABLE';
  const expTypes = bundle.experienceTypes || [];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }} className="animate-fade-up">
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)} sx={{ mb: 3, color: 'text.secondary' }}>
        Volver
      </Button>

      <Grid container spacing={4}>
        {/* Image */}
        <Grid size={{ xs: 12, lg: 7 }}>
          <Box sx={{ borderRadius: 3, overflow: 'hidden', position: 'relative' }}>
            <Box sx={{ position: 'absolute', top: 16, left: 16, zIndex: 1, display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
              {expTypes.map(type => (
                <Chip
                  key={type}
                  label={experienceLabelMap[type] || type}
                  sx={{ bgcolor: 'rgba(170,59,255,0.9)', color: 'white', fontWeight: 700 }}
                />
              ))}
            </Box>
            <img
              src={getImageUrl(expTypes)}
              alt={bundle.nameBundle}
              style={{ width: '100%', height: 480, objectFit: 'cover', display: 'block' }}
            />
          </Box>
        </Grid>

        {/* Details */}
        <Grid size={{ xs: 12, lg: 5 }}>
          <Paper sx={{ p: 4, borderRadius: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Chip
                label={bundle.stateBundle}
                color={isAvailable ? 'success' : 'error'}
                sx={{ fontWeight: 700 }}
              />
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <AccessTimeIcon sx={{ fontSize: 18, color: 'text.secondary' }} />
                <Typography variant="body2" color="text.secondary" fontWeight={600}>
                  {bundle.durationBundle} Días
                </Typography>
              </Box>
            </Box>

            <Typography variant="h3" sx={{ fontSize: '2rem', mb: 1 }}>{bundle.nameBundle}</Typography>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 3 }}>
              <LocationOnIcon sx={{ color: 'secondary.main', fontSize: 20 }} />
              <Typography variant="h6" color="text.secondary" fontWeight={400}>{bundle.destinationBundle}</Typography>
            </Box>

            <Box sx={{ mb: 3, flexGrow: 1 }}>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>Acerca de esta experiencia</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.8 }}>
                {bundle.descriptionBundle}
              </Typography>
            </Box>

            {/* Included Services, Conditions, Restrictions */}
            {bundle.includedServices && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.5 }}>
                  <CheckCircleIcon sx={{ fontSize: 16, color: 'success.main' }} /> Servicios Incluidos
                </Typography>
                <Typography variant="body2" color="text.secondary">{bundle.includedServices}</Typography>
              </Box>
            )}
            {bundle.conditions && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.5 }}>
                  <InfoOutlinedIcon sx={{ fontSize: 16, color: 'info.main' }} /> Condiciones
                </Typography>
                <Typography variant="body2" color="text.secondary">{bundle.conditions}</Typography>
              </Box>
            )}
            {bundle.restrictions && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.5 }}>
                  <BlockIcon sx={{ fontSize: 16, color: 'warning.main' }} /> Restricciones
                </Typography>
                <Typography variant="body2" color="text.secondary">{bundle.restrictions}</Typography>
              </Box>
            )}

            <Grid container spacing={2} sx={{ mb: 3, pt: 2, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
              <Grid size={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <GroupIcon sx={{ color: 'primary.main' }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Cupos Disponibles</Typography>
                    <Typography variant="body1" fontWeight={700}>{bundle.availableSlotsBundle}</Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid size={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CalendarMonthIcon sx={{ color: 'secondary.main' }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Fechas</Typography>
                    <Typography variant="body2" fontWeight={600}>
                      {new Date(bundle.startDateBundle).toLocaleDateString()} - {new Date(bundle.endDateBundle).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>

            <Paper sx={{ p: 3, borderRadius: 3, textAlign: 'center', bgcolor: 'rgba(170,59,255,0.05)', border: '1px solid rgba(170,59,255,0.15)' }}>
              <Typography variant="caption" color="text.secondary" fontWeight={700} textTransform="uppercase">
                Precio por persona
              </Typography>
              <Typography variant="h3" color="primary" sx={{ fontWeight: 800, my: 1 }}>
                ${bundle.priceBundle?.toLocaleString()} <Typography component="span" variant="body2" color="text.secondary">CLP</Typography>
              </Typography>
              <Button
                fullWidth variant="contained" size="large"
                disabled={!isAvailable}
                onClick={() => navigate(`/book/${bundle.idBundle}`)}
                sx={{ py: 1.5, fontSize: '1.05rem' }}
              >
                {isAvailable ? 'Reservar Esta Experiencia' : 'Actualmente No Disponible'}
              </Button>
            </Paper>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}

export default PackageDetails;

import React, { useState, useEffect } from 'react';
import {
  Box, Container, Typography, TextField, InputAdornment, Grid, Card,
  CardContent, CardMedia, Chip, Button, CircularProgress, IconButton
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import SpaIcon from '@mui/icons-material/Spa';
import HikingIcon from '@mui/icons-material/Hiking';
import MuseumIcon from '@mui/icons-material/Museum';
import FamilyRestroomIcon from '@mui/icons-material/FamilyRestroom';
import FavoriteIcon from '@mui/icons-material/Favorite';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';

const experienceImages = {
  RELAX: 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?q=80&w=800&auto=format&fit=crop',
  ADVENTURE: 'https://images.unsplash.com/photo-1533240332313-0db49b459ad6?q=80&w=800&auto=format&fit=crop',
  CULTURAL: 'https://images.unsplash.com/photo-1518391846015-55a9cc003b25?q=80&w=800&auto=format&fit=crop',
  FAMILY: 'https://images.unsplash.com/photo-1511895426328-dc8714191300?q=80&w=800&auto=format&fit=crop',
  ROMANTIC: 'https://images.unsplash.com/photo-1516815231560-8f41ec531527?q=80&w=800&auto=format&fit=crop',
  BUSINESS: 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=800&auto=format&fit=crop',
};
const defaultImage = 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=800&auto=format&fit=crop';

const categories = [
  { key: 'RELAX', label: 'Relajación', icon: <SpaIcon />, color: '#22c55e' },
  { key: 'ADVENTURE', label: 'Aventura', icon: <HikingIcon />, color: '#f59e0b' },
  { key: 'CULTURAL', label: 'Cultural', icon: <MuseumIcon />, color: '#3b82f6' },
  { key: 'FAMILY', label: 'Familiar', icon: <FamilyRestroomIcon />, color: '#ec4899' },
  { key: 'ROMANTIC', label: 'Romántico', icon: <FavoriteIcon />, color: '#ef4444' },
  { key: 'BUSINESS', label: 'Negocios', icon: <BusinessCenterIcon />, color: '#6366f1' },
];

function LandingPage() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [featuredBundles, setFeaturedBundles] = useState([]);
  const [upcomingBundles, setUpcomingBundles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    bundleService.searchAvailableBundles({})
      .then(data => {
        const now = new Date();

        // Featured: packages with promo active or discount > 0
        const promos = data.filter(b =>
          b.promoDiscountPercent > 0 &&
          b.promoStartDate && b.promoEndDate &&
          new Date(b.promoStartDate) <= now && new Date(b.promoEndDate) >= now
        );

        // Upcoming: packages sorted by start date (soonest first), take top 8
        const upcoming = [...data]
          .filter(b => new Date(b.startDateBundle) >= now)
          .sort((a, b) => new Date(a.startDateBundle) - new Date(b.startDateBundle))
          .slice(0, 8);

        // If no promos, just show the first 6 available bundles as featured
        setFeaturedBundles(promos.length > 0 ? promos.slice(0, 8) : data.slice(0, 6));
        setUpcomingBundles(upcoming.length > 0 ? upcoming : data.slice(0, 6));
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/catalog?search=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/catalog');
    }
  };

  const scrollCarousel = (id, direction) => {
    const el = document.getElementById(id);
    if (el) el.scrollBy({ left: direction * 340, behavior: 'smooth' });
  };

  const FeaturedCard = ({ bundle }) => (
    <Card
      sx={{
        width: 320, minWidth: 320, cursor: 'pointer', position: 'relative',
        borderRadius: 3, overflow: 'hidden',
      }}
      onClick={() => navigate(`/package/${bundle.idBundle}`)}
    >
      <CardMedia
        component="img"
        height="200"
        image={experienceImages[bundle.tipoExperienciaBundle] || defaultImage}
        alt={bundle.nameBundle}
        sx={{ objectFit: 'cover' }}
      />
      {bundle.promoDiscountPercent > 0 && (
        <Box sx={{ position: 'absolute', top: 12, left: 12 }}>
          <span className="promo-badge">
            <LocalOfferIcon sx={{ fontSize: 12, mr: 0.3, verticalAlign: 'middle' }} />
            {Math.round(bundle.promoDiscountPercent * 100)}% OFF
          </span>
        </Box>
      )}
      <CardContent sx={{ p: 2.5 }}>
        <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 0.5, display: 'flex', alignItems: 'center', gap: 0.5 }}>
          📍 {bundle.destinyBundle}
        </Typography>
        <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1rem', mb: 1, lineHeight: 1.3 }}>
          {bundle.nameBundle}
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6" color="primary" sx={{ fontWeight: 800, fontSize: '1.1rem' }}>
            ${bundle.priceBundle?.toLocaleString()} <Typography component="span" variant="caption" color="text.secondary">CLP</Typography>
          </Typography>
          <Chip label={bundle.tipoExperienciaBundle} size="small" variant="outlined" color="primary" />
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box>
      {/* ========== HERO SECTION ========== */}
      <Box className="hero-bg" sx={{ pt: { xs: 10, md: 16 }, pb: { xs: 8, md: 14 }, position: 'relative', textAlign: 'center' }}>
        <Container maxWidth="md" sx={{ position: 'relative', zIndex: 1 }}>
          <Box className="animate-fade-up">
            <Chip
              icon={<FlightTakeoffIcon />}
              label="Descubre el mundo con nosotros"
              sx={{ mb: 3, bgcolor: 'rgba(170,59,255,0.1)', border: '1px solid rgba(170,59,255,0.3)', color: 'primary.light' }}
            />
            <Typography variant="h1" sx={{ fontSize: { xs: '2.5rem', md: '4rem' }, mb: 2, lineHeight: 1.1 }}>
              Tu Próxima <span className="gradient-text">Aventura</span><br />Comienza Aquí
            </Typography>
            <Typography variant="h6" color="text.secondary" sx={{ mb: 5, fontWeight: 400, maxWidth: 600, mx: 'auto' }}>
              Explora experiencias de viaje curadas, reserva tu viaje soñado y crea recuerdos que durarán toda la vida.
            </Typography>
          </Box>

          {/* Search Bar */}
          <Box
            component="form"
            onSubmit={handleSearch}
            className="animate-fade-up"
            sx={{
              maxWidth: 600, mx: 'auto',
              animation: 'fadeUp 0.6s ease-out 0.2s both',
            }}
          >
            <TextField
              fullWidth
              placeholder="Buscar paquetes por nombre, destino..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <Button type="submit" variant="contained" size="small" sx={{ borderRadius: 2, minWidth: 100 }}>
                      Buscar
                    </Button>
                  </InputAdornment>
                ),
                sx: {
                  bgcolor: 'rgba(255,255,255,0.05)',
                  borderRadius: 3, py: 0.5,
                  '& fieldset': { borderColor: 'rgba(255,255,255,0.1)' },
                  '&:hover fieldset': { borderColor: 'rgba(170,59,255,0.4) !important' },
                },
              }}
            />
          </Box>
        </Container>
      </Box>

      {/* ========== FEATURED / PROMO SECTION ========== */}
      <Container maxWidth="lg" sx={{ py: 6 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <LocalOfferIcon color="primary" /> Paquetes Destacados
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Ofertas y promociones que no te querrás perder
            </Typography>
          </Box>
          <Box>
            <IconButton onClick={() => scrollCarousel('featured-carousel', -1)} sx={{ color: 'text.secondary' }}>
              <ChevronLeftIcon />
            </IconButton>
            <IconButton onClick={() => scrollCarousel('featured-carousel', 1)} sx={{ color: 'text.secondary' }}>
              <ChevronRightIcon />
            </IconButton>
          </Box>
        </Box>

        {loading ? (
          <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
        ) : (
          <Box id="featured-carousel" className="carousel-scroll">
            {featuredBundles.map(b => <FeaturedCard key={b.idBundle} bundle={b} />)}
          </Box>
        )}
      </Container>

      {/* ========== UPCOMING DEPARTURES ========== */}
      <Container maxWidth="lg" sx={{ py: 6 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AccessTimeIcon color="secondary" /> Próximas Salidas
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Viajes que salen pronto — ¡reserva antes de que se agoten!
            </Typography>
          </Box>
          <Box>
            <IconButton onClick={() => scrollCarousel('upcoming-carousel', -1)} sx={{ color: 'text.secondary' }}>
              <ChevronLeftIcon />
            </IconButton>
            <IconButton onClick={() => scrollCarousel('upcoming-carousel', 1)} sx={{ color: 'text.secondary' }}>
              <ChevronRightIcon />
            </IconButton>
          </Box>
        </Box>

        {loading ? (
          <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
        ) : (
          <Box id="upcoming-carousel" className="carousel-scroll">
            {upcomingBundles.map(b => <FeaturedCard key={b.idBundle} bundle={b} />)}
          </Box>
        )}
      </Container>

      {/* ========== CATEGORIES SECTION ========== */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box sx={{ textAlign: 'center', mb: 5 }}>
          <Typography variant="h4" sx={{ mb: 1 }}>Explora por Experiencia</Typography>
          <Typography variant="body1" color="text.secondary">Elige tu estilo de viaje</Typography>
        </Box>
        <Grid container spacing={3} justifyContent="center" className="stagger-children">
          {categories.map(cat => (
            <Grid size={{ xs: 6, sm: 4, md: 2 }} key={cat.key}>
              <Card
                sx={{
                  textAlign: 'center', p: 3, cursor: 'pointer',
                  '&:hover .cat-icon': { transform: 'scale(1.2)', color: cat.color },
                }}
                onClick={() => navigate(`/catalog?experience=${cat.key}`)}
              >
                <Box className="cat-icon" sx={{ fontSize: 40, mb: 1.5, color: 'text.secondary', transition: 'all 0.3s' }}>
                  {cat.icon}
                </Box>
                <Typography variant="body2" fontWeight={600}>{cat.label}</Typography>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* ========== CTA SECTION ========== */}
      <Box sx={{
        py: 10, textAlign: 'center',
        background: 'linear-gradient(135deg, rgba(170,59,255,0.08) 0%, rgba(0,188,212,0.05) 100%)',
        borderTop: '1px solid rgba(255,255,255,0.04)',
      }}>
        <Container maxWidth="sm">
          <Typography variant="h4" sx={{ mb: 2 }}>¿Listo para Explorar?</Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Explora nuestro catálogo completo de paquetes de viaje con filtros avanzados.
          </Typography>
          <Button
            variant="contained"
            size="large"
            endIcon={<ArrowForwardIcon />}
            onClick={() => navigate('/catalog')}
            sx={{ py: 1.5, px: 5, fontSize: '1rem' }}
          >
            Ver Todos los Paquetes
          </Button>
        </Container>
      </Box>
    </Box>
  );
}

export default LandingPage;

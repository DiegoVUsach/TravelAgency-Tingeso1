import React from 'react';
import { Card, CardMedia, CardContent, Typography, Box, Chip, Button } from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import GroupIcon from '@mui/icons-material/Group';
import { useNavigate } from 'react-router-dom';

const experienceImages = {
  RELAX: 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?q=80&w=600&auto=format&fit=crop',
  ADVENTURE: 'https://images.unsplash.com/photo-1533240332313-0db49b459ad6?q=80&w=600&auto=format&fit=crop',
  CULTURAL: 'https://images.unsplash.com/photo-1518391846015-55a9cc003b25?q=80&w=600&auto=format&fit=crop',
  FAMILY: 'https://images.unsplash.com/photo-1511895426328-dc8714191300?q=80&w=600&auto=format&fit=crop',
  ROMANTIC: 'https://images.unsplash.com/photo-1516815231560-8f41ec531527?q=80&w=600&auto=format&fit=crop',
  BUSINESS: 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=600&auto=format&fit=crop',
  NATURE: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=600&auto=format&fit=crop',
  CULINARY: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=600&auto=format&fit=crop',
  WELLNESS: 'https://images.unsplash.com/photo-1544161515-4ab6ce6db874?q=80&w=600&auto=format&fit=crop',
  NIGHTLIFE: 'https://images.unsplash.com/photo-1566417713940-fe7c737a9ef2?q=80&w=600&auto=format&fit=crop',
};
const defaultImage = 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=600&auto=format&fit=crop';

const experienceLabelMap = {
  RELAX: 'Relajación', ADVENTURE: 'Aventura', CULTURAL: 'Cultural',
  FAMILY: 'Familiar', ROMANTIC: 'Romántico', BUSINESS: 'Negocios',
  NATURE: 'Naturaleza', CULINARY: 'Gastronómico', WELLNESS: 'Bienestar',
  NIGHTLIFE: 'Vida Nocturna',
};

const seasonLabelMap = {
  SUMMER: 'Verano', AUTUMN: 'Otoño', WINTER: 'Invierno',
  SPRING: 'Primavera', ALL_YEAR: 'Todo el Año',
};

const categoryLabelMap = {
  ECONOMIC: 'Económico', STANDARD: 'Estándar',
  PREMIUM: 'Premium', LUXURY: 'Lujo',
};

function BundleCard({ bundle }) {
  const navigate = useNavigate();
  const expTypes = bundle.experienceTypes || [];
  const firstType = expTypes.length > 0 ? expTypes[0] : null;

  return (
    <Card
      sx={{ cursor: 'pointer', borderRadius: 3, overflow: 'hidden', height: '100%', display: 'flex', flexDirection: 'column' }}
      onClick={() => navigate(`/package/${bundle.idBundle}`)}
    >
      <Box sx={{ position: 'relative' }}>
        <CardMedia
          component="img"
          height="180"
          image={experienceImages[firstType] || defaultImage}
          alt={bundle.nameBundle}
          sx={{ objectFit: 'cover' }}
        />
        <Chip
          label={bundle.stateBundle}
          size="small"
          color={bundle.stateBundle === 'AVAILABLE' ? 'success' : 'error'}
          sx={{ position: 'absolute', top: 12, right: 12, fontWeight: 700 }}
        />
        {bundle.promoDiscountPercent > 0 && (
          <Box sx={{ position: 'absolute', top: 12, left: 12 }}>
            <span className="promo-badge">{Math.round(bundle.promoDiscountPercent * 100)}% OFF</span>
          </Box>
        )}
      </Box>

      <CardContent sx={{ p: 2.5, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.5 }}>
          <LocationOnIcon sx={{ fontSize: 16, color: 'secondary.main' }} />
          <Typography variant="caption" color="text.secondary">{bundle.destinationBundle}</Typography>
        </Box>

        <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1, lineHeight: 1.3 }}>
          {bundle.nameBundle}
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 1, flexGrow: 1, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
          {bundle.descriptionBundle}
        </Typography>

        {/* Experience Type Chips */}
        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mb: 1 }}>
          {expTypes.map(type => (
            <Chip key={type} label={experienceLabelMap[type] || type} size="small" variant="outlined" color="primary" sx={{ fontSize: '0.65rem', height: 22 }} />
          ))}
        </Box>

        {/* Season & Category Chips */}
        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mb: 2 }}>
          {bundle.seasonType && (
            <Chip label={seasonLabelMap[bundle.seasonType] || bundle.seasonType} size="small" variant="outlined" color="success" sx={{ fontSize: '0.65rem', height: 22 }} />
          )}
          {bundle.categoryType && (
            <Chip label={categoryLabelMap[bundle.categoryType] || bundle.categoryType} size="small" variant="outlined" color="secondary" sx={{ fontSize: '0.65rem', height: 22 }} />
          )}
        </Box>

        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <AccessTimeIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
            <Typography variant="caption" color="text.secondary">{bundle.durationBundle} días</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <GroupIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
            <Typography variant="caption" color="text.secondary">{bundle.availableSlotsBundle} cupos</Typography>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pt: 2, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
          <Typography variant="h6" color="primary" sx={{ fontWeight: 800 }}>
            ${bundle.priceBundle?.toLocaleString()} <Typography component="span" variant="caption" color="text.secondary">CLP</Typography>
          </Typography>
          <Button variant="outlined" size="small" sx={{ borderRadius: 2 }}>
            Ver Detalles
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
}

export default BundleCard;
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
};
const defaultImage = 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=600&auto=format&fit=crop';

function BundleCard({ bundle }) {
  const navigate = useNavigate();

  return (
    <Card
      sx={{ cursor: 'pointer', borderRadius: 3, overflow: 'hidden', height: '100%', display: 'flex', flexDirection: 'column' }}
      onClick={() => navigate(`/package/${bundle.idBundle}`)}
    >
      <Box sx={{ position: 'relative' }}>
        <CardMedia
          component="img"
          height="180"
          image={experienceImages[bundle.tipoExperienciaBundle] || defaultImage}
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
          <Typography variant="caption" color="text.secondary">{bundle.destinyBundle}</Typography>
        </Box>

        <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1, lineHeight: 1.3 }}>
          {bundle.nameBundle}
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 2, flexGrow: 1, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
          {bundle.descBundle}
        </Typography>

        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <AccessTimeIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
            <Typography variant="caption" color="text.secondary">{bundle.durationBundle} days</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <GroupIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
            <Typography variant="caption" color="text.secondary">{bundle.availableSlotsBundle} spots</Typography>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pt: 2, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
          <Typography variant="h6" color="primary" sx={{ fontWeight: 800 }}>
            ${bundle.priceBundle?.toLocaleString()} <Typography component="span" variant="caption" color="text.secondary">CLP</Typography>
          </Typography>
          <Button variant="outlined" size="small" sx={{ borderRadius: 2 }}>
            View Details
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
}

export default BundleCard;
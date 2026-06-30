import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, TextField, Button, Box, Grid, Alert,
  CircularProgress, MenuItem, Select, FormControl, InputLabel, Checkbox, ListItemText,
  OutlinedInput
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { useAuth } from '../context/AuthProvider';

const EXPERIENCE_OPTIONS = [
  { value: 'RELAX', label: 'Relajación' },
  { value: 'ADVENTURE', label: 'Aventura' },
  { value: 'CULTURAL', label: 'Cultural' },
  { value: 'FAMILY', label: 'Familiar' },
  { value: 'ROMANTIC', label: 'Romántico' },
  { value: 'BUSINESS', label: 'Negocios' },
  { value: 'NATURE', label: 'Naturaleza' },
  { value: 'CULINARY', label: 'Gastronómico' },
  { value: 'WELLNESS', label: 'Bienestar' },
  { value: 'NIGHTLIFE', label: 'Vida Nocturna' },
];

const SEASON_OPTIONS = [
  { value: 'SUMMER', label: 'Verano' },
  { value: 'AUTUMN', label: 'Otoño' },
  { value: 'WINTER', label: 'Invierno' },
  { value: 'SPRING', label: 'Primavera' },
  { value: 'ALL_YEAR', label: 'Todo el Año' },
];

const CATEGORY_OPTIONS = [
  { value: 'ECONOMIC', label: 'Económico' },
  { value: 'STANDARD', label: 'Estándar' },
  { value: 'PREMIUM', label: 'Premium' },
  { value: 'LUXURY', label: 'Lujo' },
];

function AdminPackageForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role } = useAuth();
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    nameBundle: '', destinationBundle: '', descriptionBundle: '', priceBundle: '',
    availableSlotsBundle: '', startDateBundle: '', endDateBundle: '',
    stateBundle: 'AVAILABLE', experienceTypes: ['RELAX'],
    seasonType: '', categoryType: '',
    includedServices: '', conditions: '', restrictions: ''
  });
  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEditMode) {
      bundleService.getBundleById(id)
        .then(data => {
          setFormData({
            ...data,
            startDateBundle: data.startDateBundle ? data.startDateBundle.split('T')[0] : '',
            endDateBundle: data.endDateBundle ? data.endDateBundle.split('T')[0] : '',
            experienceTypes: data.experienceTypes || ['RELAX'],
            seasonType: data.seasonType || '',
            categoryType: data.categoryType || '',
            includedServices: data.includedServices || '',
            conditions: data.conditions || '',
            restrictions: data.restrictions || '',
          });
          setLoading(false);
        })
        .catch(() => { setError('No se pudieron obtener los detalles del paquete.'); setLoading(false); });
    }
  }, [id, isEditMode]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleExperienceChange = (event) => {
    const { value } = event.target;
    setFormData(prev => ({ ...prev, experienceTypes: typeof value === 'string' ? value.split(',') : value }));
  };

  const validateForm = () => {
    if (Number(formData.priceBundle) <= 0) { setError('El precio debe ser mayor que cero.'); return false; }
    if (Number(formData.availableSlotsBundle) <= 0) { setError('Los cupos totales deben ser mayor que cero.'); return false; }
    if (new Date(formData.endDateBundle) <= new Date(formData.startDateBundle)) { setError('La fecha de llegada debe ser después de la fecha de salida.'); return false; }
    if (!formData.experienceTypes || formData.experienceTypes.length === 0) { setError('Debes seleccionar al menos un tipo de experiencia.'); return false; }
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    setSaving(true);
    setError(null);
    // Duration is auto-calculated in the backend, no need to send it
    const payload = {
      ...formData,
      priceBundle: Number(formData.priceBundle),
      availableSlotsBundle: Number(formData.availableSlotsBundle),
      seasonType: formData.seasonType || null,
      categoryType: formData.categoryType || null,
    };
    const request = isEditMode ? bundleService.updateBundle(id, payload) : bundleService.createBundle(payload);
    request
      .then(() => navigate('/admin/dashboard'))
      .catch(err => { setError(err.response?.data?.message || 'No se pudo guardar el paquete.'); setSaving(false); });
  };

  if (role !== 'ADMIN') {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
        <Paper sx={{ p: 5, borderRadius: 3 }}>
          <Typography variant="h5" color="error">Acceso Denegado</Typography>
        </Paper>
      </Container>
    );
  }

  if (loading) return <Box sx={{ textAlign: 'center', py: 10 }}><CircularProgress /></Box>;

  return (
    <Container maxWidth="md" sx={{ py: 4 }} className="animate-fade-up">
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/admin/dashboard')} sx={{ mb: 3, color: 'text.secondary' }}>
        Volver al Panel
      </Button>

      <Paper sx={{ p: 4, borderRadius: 3 }}>
        <Typography variant="h4" sx={{ mb: 3 }}>{isEditMode ? 'Editar Paquete' : 'Crear Nuevo Paquete'}</Typography>
        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Grid container spacing={2}>
            {/* Basic Info */}
            <Grid size={6}>
              <TextField fullWidth required label="Nombre del Paquete" name="nameBundle" value={formData.nameBundle} onChange={handleInputChange} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required label="Destino" name="destinationBundle" value={formData.destinationBundle} onChange={handleInputChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth required multiline rows={3} label="Descripción Detallada" name="descriptionBundle" value={formData.descriptionBundle} onChange={handleInputChange} />
            </Grid>

            {/* Price & Slots */}
            <Grid size={6}>
              <TextField fullWidth required type="number" label="Precio (CLP)" name="priceBundle" value={formData.priceBundle} onChange={handleInputChange} inputProps={{ min: 1 }} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required type="number" label="Cupos Totales" name="availableSlotsBundle" value={formData.availableSlotsBundle} onChange={handleInputChange} inputProps={{ min: 1 }} />
            </Grid>

            {/* Dates (duration is auto-calculated) */}
            <Grid size={6}>
              <TextField fullWidth required type="date" label="Fecha de Salida" name="startDateBundle" value={formData.startDateBundle} onChange={handleInputChange} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required type="date" label="Fecha de Llegada" name="endDateBundle" value={formData.endDateBundle} onChange={handleInputChange} InputLabelProps={{ shrink: true }} />
            </Grid>

            {/* Auto-calculated duration display */}
            {formData.startDateBundle && formData.endDateBundle && new Date(formData.endDateBundle) > new Date(formData.startDateBundle) && (
              <Grid size={12}>
                <Alert severity="info" sx={{ py: 0.5 }}>
                  Duración calculada automáticamente: <strong>
                    {Math.round((new Date(formData.endDateBundle) - new Date(formData.startDateBundle)) / (1000 * 60 * 60 * 24))} días
                  </strong>
                </Alert>
              </Grid>
            )}

            {/* Detail & Classification */}
            <Grid size={12}>
              <Typography variant="subtitle2" sx={{ mt: 1, mb: 0.5, color: 'text.secondary' }}>Detalle y Clasificación</Typography>
            </Grid>
            <Grid size={12}>
              <TextField fullWidth multiline rows={2} label="Servicios Incluidos" name="includedServices" value={formData.includedServices} onChange={handleInputChange}
                placeholder="Ej: Hotel 5 estrellas, traslados aeropuerto, guía bilingüe, desayuno buffet..." />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth multiline rows={2} label="Condiciones" name="conditions" value={formData.conditions} onChange={handleInputChange}
                placeholder="Ej: Sujeto a disponibilidad, mínimo 2 personas..." />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth multiline rows={2} label="Restricciones" name="restrictions" value={formData.restrictions} onChange={handleInputChange}
                placeholder="Ej: No reembolsable, edad mínima 18 años..." />
            </Grid>

            {/* Experience Types (multi-select) */}
            <Grid size={12}>
              <Typography variant="subtitle2" sx={{ mt: 1, mb: 0.5, color: 'text.secondary' }}>Etiquetas</Typography>
            </Grid>
            <Grid size={4}>
              <FormControl fullWidth>
                <InputLabel>Tipos de Experiencia</InputLabel>
                <Select
                  multiple
                  name="experienceTypes"
                  value={formData.experienceTypes || []}
                  onChange={handleExperienceChange}
                  input={<OutlinedInput label="Tipos de Experiencia" />}
                  renderValue={(selected) => selected.map(v => EXPERIENCE_OPTIONS.find(o => o.value === v)?.label || v).join(', ')}
                >
                  {EXPERIENCE_OPTIONS.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>
                      <Checkbox checked={(formData.experienceTypes || []).includes(opt.value)} />
                      <ListItemText primary={opt.label} />
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Season Type (single-select) */}
            <Grid size={4}>
              <FormControl fullWidth>
                <InputLabel>Temporada</InputLabel>
                <Select name="seasonType" value={formData.seasonType} onChange={handleInputChange} label="Temporada">
                  <MenuItem value="">Sin Especificar</MenuItem>
                  {SEASON_OPTIONS.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Category Type (single-select) */}
            <Grid size={4}>
              <FormControl fullWidth>
                <InputLabel>Categoría</InputLabel>
                <Select name="categoryType" value={formData.categoryType} onChange={handleInputChange} label="Categoría">
                  <MenuItem value="">Sin Especificar</MenuItem>
                  {CATEGORY_OPTIONS.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* State */}
            <Grid size={6}>
              <FormControl fullWidth>
                <InputLabel>Estado</InputLabel>
                <Select name="stateBundle" value={formData.stateBundle} onChange={handleInputChange} label="Estado">
                  <MenuItem value="AVAILABLE">Disponible</MenuItem>
                  <MenuItem value="SOLD_OUT">Agotado</MenuItem>
                  <MenuItem value="CANCELED">Cancelado</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>

          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 4, pt: 3, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
            <Button variant="outlined" onClick={() => navigate('/admin/dashboard')} disabled={saving}>Cancelar</Button>
            <Button variant="contained" type="submit" startIcon={<SaveIcon />} disabled={saving}>
              {saving ? <CircularProgress size={20} /> : 'Guardar Paquete'}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
}

export default AdminPackageForm;

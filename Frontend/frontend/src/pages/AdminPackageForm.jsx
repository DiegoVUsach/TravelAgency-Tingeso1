import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, TextField, Button, Box, Grid, Alert,
  CircularProgress, MenuItem, Select, FormControl, InputLabel
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { useParams, useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { useAuth } from '../context/AuthProvider';

function AdminPackageForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role } = useAuth();
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    nameBundle: '', destinyBundle: '', descBundle: '', priceBundle: '',
    availableSlotsBundle: '', startDateBundle: '', endDateBundle: '',
    durationBundle: '', stateBundle: 'AVAILABLE', tipoExperienciaBundle: 'RELAX',
    canBeModified: true
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

  const validateForm = () => {
    if (Number(formData.priceBundle) <= 0) { setError('El precio debe ser mayor que cero.'); return false; }
    if (Number(formData.availableSlotsBundle) <= 0) { setError('Los cupos totales deben ser mayor que cero.'); return false; }
    if (new Date(formData.endDateBundle) <= new Date(formData.startDateBundle)) { setError('La fecha de llegada debe ser después de la fecha de salida.'); return false; }
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    setSaving(true);
    setError(null);
    const payload = { ...formData, priceBundle: Number(formData.priceBundle), availableSlotsBundle: Number(formData.availableSlotsBundle), durationBundle: Number(formData.durationBundle), canBeModified: formData.canBeModified ?? true };
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
            <Grid size={6}>
              <TextField fullWidth required label="Nombre del Paquete" name="nameBundle" value={formData.nameBundle} onChange={handleInputChange} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required label="Destino" name="destinyBundle" value={formData.destinyBundle} onChange={handleInputChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth required multiline rows={3} label="Descripción" name="descBundle" value={formData.descBundle} onChange={handleInputChange} />
            </Grid>
            <Grid size={4}>
              <TextField fullWidth required type="number" label="Precio (CLP)" name="priceBundle" value={formData.priceBundle} onChange={handleInputChange} inputProps={{ min: 1 }} />
            </Grid>
            <Grid size={4}>
              <TextField fullWidth required type="number" label="Cupos Totales" name="availableSlotsBundle" value={formData.availableSlotsBundle} onChange={handleInputChange} inputProps={{ min: 1 }} />
            </Grid>
            <Grid size={4}>
              <TextField fullWidth required type="number" label="Duración (Días)" name="durationBundle" value={formData.durationBundle} onChange={handleInputChange} inputProps={{ min: 1 }} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required type="date" label="Fecha de Salida" name="startDateBundle" value={formData.startDateBundle} onChange={handleInputChange} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid size={6}>
              <TextField fullWidth required type="date" label="Fecha de Llegada" name="endDateBundle" value={formData.endDateBundle} onChange={handleInputChange} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid size={6}>
              <FormControl fullWidth>
                <InputLabel>Tipo de Experiencia</InputLabel>
                <Select name="tipoExperienciaBundle" value={formData.tipoExperienciaBundle || 'RELAX'} onChange={handleInputChange} label="Tipo de Experiencia">
                  <MenuItem value="RELAX">Relajación</MenuItem>
                  <MenuItem value="ADVENTURE">Aventura</MenuItem>
                  <MenuItem value="CULTURAL">Cultural</MenuItem>
                  <MenuItem value="FAMILY">Familiar</MenuItem>
                  <MenuItem value="ROMANTIC">Romántico</MenuItem>
                  <MenuItem value="BUSINESS">Negocios</MenuItem>
                </Select>
              </FormControl>
            </Grid>
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

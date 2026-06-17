import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, TextField, Button, Box, Alert, CircularProgress, Avatar, Divider
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { useAuth } from '../context/AuthProvider';
import { userService } from '../services/userService';
import { useNavigate } from 'react-router-dom';

function MyProfile() {
  const { localUser, refreshProfile, logout } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({ fullName: '', phone: '', identityDocument: '', nationality: '' });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    if (localUser) {
      setFormData({
        fullName: localUser.fullName || '',
        phone: localUser.phone || '',
        identityDocument: localUser.identityDocument || '',
        nationality: localUser.nationality || '',
      });
    }
  }, [localUser]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });
    try {
      await userService.updateMyProfile(formData);
      await refreshProfile();
      setMessage({ type: 'success', text: '¡Perfil actualizado exitosamente!' });
    } catch {
      setMessage({ type: 'error', text: 'No se pudo actualizar el perfil.' });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (window.confirm('¿Estás seguro de que quieres desactivar tu cuenta?')) {
      try {
        await userService.deleteMyAccount();
        alert('Cuenta desactivada.');
        logout();
        navigate('/');
      } catch {
        setMessage({ type: 'error', text: 'No se pudo desactivar la cuenta.' });
      }
    }
  };

  if (!localUser) return <Box sx={{ textAlign: 'center', py: 10 }}><CircularProgress /></Box>;

  const initials = (localUser.fullName || 'U').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();

  return (
    <Container maxWidth="sm" sx={{ py: 5 }} className="animate-fade-up">
      <Paper sx={{ p: 4, borderRadius: 3 }}>
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <Avatar sx={{ width: 64, height: 64, mx: 'auto', mb: 2, bgcolor: 'primary.main', fontSize: '1.5rem', fontWeight: 700 }}>
            {initials}
          </Avatar>
          <Typography variant="h5">Mi Perfil</Typography>
        </Box>

        {message.text && <Alert severity={message.type} sx={{ mb: 3 }}>{message.text}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField fullWidth label="Email (solo lectura)" value={localUser.email} disabled sx={{ mb: 2 }} />
          <TextField fullWidth label="Nombre Completo" name="fullName" value={formData.fullName} onChange={handleInputChange} sx={{ mb: 2 }} />
          <TextField fullWidth label="Teléfono" name="phone" value={formData.phone} onChange={handleInputChange} sx={{ mb: 2 }} />
          <TextField fullWidth label="Documento de Identidad" name="identityDocument" value={formData.identityDocument} onChange={handleInputChange} sx={{ mb: 2 }} />
          <TextField fullWidth label="Nacionalidad" name="nationality" value={formData.nationality} onChange={handleInputChange} sx={{ mb: 3 }} />
          <Button fullWidth variant="contained" type="submit" disabled={loading} size="large">
            {loading ? <CircularProgress size={20} /> : 'Actualizar Perfil'}
          </Button>
        </Box>

        <Divider sx={{ my: 3 }} />

        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>Zona de Peligro</Typography>
          <Button variant="outlined" color="error" size="small" startIcon={<DeleteForeverIcon />} onClick={handleDeleteAccount}>
            Desactivar Cuenta
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}

export default MyProfile;

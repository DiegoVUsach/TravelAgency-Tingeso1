import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  Chip, Button, CircularProgress, Alert, IconButton, Menu, MenuItem
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import RefreshIcon from '@mui/icons-material/Refresh';
import { reservationService } from '../services/reservationService';
import { useAuth } from '../context/AuthProvider';

function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { role } = useAuth();
  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedId, setSelectedId] = useState(null);

  useEffect(() => { fetchReservations(); }, []);

  const fetchReservations = () => {
    setLoading(true);
    reservationService.getAllReservations()
      .then(data => { setReservations(data); setLoading(false); })
      .catch(() => { setError('No se pudieron obtener todas las reservas.'); setLoading(false); });
  };

  const handleMenuOpen = (e, id) => { setAnchorEl(e.currentTarget); setSelectedId(id); };
  const handleMenuClose = () => { setAnchorEl(null); setSelectedId(null); };

  const handleStateChange = (newState) => {
    if (window.confirm(`¿Cambiar la reserva #${selectedId} a ${newState}?`)) {
      reservationService.updateReservationState(selectedId, newState)
        .then(() => fetchReservations())
        .catch(err => alert(err.response?.data?.message || 'No se pudo actualizar el estado.'));
    }
    handleMenuClose();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING_PAYMENT': return 'warning';
      case 'CANCELED': return 'error';
      default: return 'default';
    }
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

  return (
    <Container maxWidth="xl" sx={{ py: 4 }} className="animate-fade-up">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4">Todas las Reservas</Typography>
          <Typography variant="body2" color="text.secondary">Gestiona las reservas y pagos en todo el sistema.</Typography>
        </Box>
        <Button variant="outlined" startIcon={<RefreshIcon />} onClick={fetchReservations} disabled={loading}>
          Refrescar
        </Button>
      </Box>

      {loading && <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Email del Cliente</TableCell>
                <TableCell>Paquete</TableCell>
                <TableCell>Pasajeros</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Gestionar</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {reservations.map(res => (
                <TableRow key={res.id} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{res.id}</TableCell>
                  <TableCell>{res.user?.email || 'N/A'}</TableCell>
                  <TableCell sx={{ fontWeight: 600, maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {res.bundle?.nameBundle || 'Desconocido'}
                  </TableCell>
                  <TableCell>{res.numberOfPassengers}</TableCell>
                  <TableCell>${res.totalAmount?.toLocaleString()}</TableCell>
                  <TableCell>
                    <Chip label={res.state} size="small" color={getStatusColor(res.state)} sx={{ fontWeight: 700 }} />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton size="small" onClick={(e) => handleMenuOpen(e, res.id)}>
                      <MoreVertIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {reservations.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    No se encontraron reservas.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>

          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
            <MenuItem onClick={() => handleStateChange('CONFIRMED')}>Marcar como CONFIRMADO</MenuItem>
            <MenuItem onClick={() => handleStateChange('PENDING_PAYMENT')}>Marcar como PENDIENTE</MenuItem>
            <MenuItem onClick={() => handleStateChange('CANCELED')} sx={{ color: 'error.main' }}>Cancelar Reserva</MenuItem>
          </Menu>
        </Paper>
      )}
    </Container>
  );
}

export default AdminReservations;

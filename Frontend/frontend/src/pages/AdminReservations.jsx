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
      .catch(() => { setError('Could not fetch all reservations.'); setLoading(false); });
  };

  const handleMenuOpen = (e, id) => { setAnchorEl(e.currentTarget); setSelectedId(id); };
  const handleMenuClose = () => { setAnchorEl(null); setSelectedId(null); };

  const handleStateChange = (newState) => {
    if (window.confirm(`Change reservation #${selectedId} to ${newState}?`)) {
      reservationService.updateReservationState(selectedId, newState)
        .then(() => fetchReservations())
        .catch(err => alert(err.response?.data?.message || 'Failed to update state.'));
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
          <Typography variant="h5" color="error">Access Denied</Typography>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }} className="animate-fade-up">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4">All Reservations</Typography>
          <Typography variant="body2" color="text.secondary">Manage system-wide bookings and payments.</Typography>
        </Box>
        <Button variant="outlined" startIcon={<RefreshIcon />} onClick={fetchReservations} disabled={loading}>
          Refresh
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
                <TableCell>Client Email</TableCell>
                <TableCell>Package</TableCell>
                <TableCell>Pax</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Manage</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {reservations.map(res => (
                <TableRow key={res.id} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{res.id}</TableCell>
                  <TableCell>{res.user?.email || 'N/A'}</TableCell>
                  <TableCell sx={{ fontWeight: 600, maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {res.bundle?.nameBundle || 'Unknown'}
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
                    No reservations found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>

          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
            <MenuItem onClick={() => handleStateChange('CONFIRMED')}>Mark as CONFIRMED</MenuItem>
            <MenuItem onClick={() => handleStateChange('PENDING_PAYMENT')}>Mark as PENDING</MenuItem>
            <MenuItem onClick={() => handleStateChange('CANCELED')} sx={{ color: 'error.main' }}>Cancel Reservation</MenuItem>
          </Menu>
        </Paper>
      )}
    </Container>
  );
}

export default AdminReservations;

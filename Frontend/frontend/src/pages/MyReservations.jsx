import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  Chip, Button, CircularProgress, Alert
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { reservationService } from '../services/reservationService';

function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => { fetchReservations(); }, []);

  const fetchReservations = () => {
    setLoading(true);
    reservationService.getMyReservations()
      .then(data => { setReservations(data); setLoading(false); })
      .catch(() => { setError('No se pudieron obtener tus reservas.'); setLoading(false); });
  };

  const handleDownloadReceipt = (id) => {
    reservationService.getReceipt(id)
      .then(receiptData => {
        const receiptText = `
==============================
   RECIBO DE AGENCIA DE VIAJES
==============================
Código de Recibo: ${receiptData.receiptCode}
Fecha de Emisión: ${receiptData.issueDate}
Email del Cliente: ${receiptData.clientEmail}
Paquete: ${receiptData.bundleName}
Pasajeros: ${receiptData.numberOfPassengers}
Monto Pagado: $${receiptData.totalPaid}
Estado: ${receiptData.status}
==============================
¡Gracias por su compra!
        `;
        const blob = new Blob([receiptText], { type: 'text/plain' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `receipt_${id}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      })
      .catch(() => alert('No se pudo generar el recibo. Asegúrate de que la reserva esté CONFIRMADA.'));
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING_PAYMENT': return 'warning';
      case 'CANCELED': return 'error';
      default: return 'default';
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }} className="animate-fade-up">
      <Typography variant="h4" sx={{ mb: 1 }}>Mis Reservas</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>Sigue tus reservas y descarga los recibos.</Typography>

      {loading && <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Paquete</TableCell>
                <TableCell>Pasajeros</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {reservations.map(res => (
                <TableRow key={res.id} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{res.id}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{res.bundle?.nameBundle || 'Desconocido'}</TableCell>
                  <TableCell>{res.numberOfPassengers}</TableCell>
                  <TableCell>${res.totalAmount?.toLocaleString()}</TableCell>
                  <TableCell>
                    <Chip label={res.state} size="small" color={getStatusColor(res.state)} sx={{ fontWeight: 700 }} />
                  </TableCell>
                  <TableCell align="right">
                    {res.state === 'CONFIRMED' && (
                      <Button
                        size="small" variant="outlined" startIcon={<DownloadIcon />}
                        onClick={() => handleDownloadReceipt(res.id)}
                      >
                        Recibo
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {reservations.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    No tienes reservas aún.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Container>
  );
}

export default MyReservations;

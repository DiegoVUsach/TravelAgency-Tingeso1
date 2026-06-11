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
      .catch(() => { setError('Could not fetch your reservations.'); setLoading(false); });
  };

  const handleDownloadReceipt = (id) => {
    reservationService.getReceipt(id)
      .then(receiptData => {
        const receiptText = `
==============================
   TRAVEL AGENCY RECEIPT
==============================
Receipt Code: ${receiptData.receiptCode}
Issue Date: ${receiptData.issueDate}
Client Email: ${receiptData.clientEmail}
Package: ${receiptData.bundleName}
Passengers: ${receiptData.numberOfPassengers}
Amount Paid: $${receiptData.totalPaid}
Status: ${receiptData.status}
==============================
Thank you for your purchase!
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
      .catch(() => alert('Could not generate receipt. Ensure the reservation is CONFIRMED.'));
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
      <Typography variant="h4" sx={{ mb: 1 }}>My Reservations</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>Track your bookings and download receipts.</Typography>

      {loading && <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Package</TableCell>
                <TableCell>Passengers</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {reservations.map(res => (
                <TableRow key={res.id} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{res.id}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{res.bundle?.nameBundle || 'Unknown'}</TableCell>
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
                        Receipt
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {reservations.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    You don't have any reservations yet.
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

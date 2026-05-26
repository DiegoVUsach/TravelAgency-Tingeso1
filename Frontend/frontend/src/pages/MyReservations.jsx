import React, { useState, useEffect } from 'react';
import { Container, Table, Badge, Button, Spinner, Alert } from 'react-bootstrap';
import { reservationService } from '../services/reservationService';

function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = () => {
    setLoading(true);
    reservationService.getMyReservations()
      .then(data => {
        setReservations(data);
        setLoading(false);
      })
      .catch(err => {
        setError('Could not fetch your reservations.');
        setLoading(false);
      });
  };

  const handleDownloadReceipt = (id) => {
    reservationService.getReceipt(id)
      .then(receiptData => {
        // Mocking a PDF download by generating a text blob
        const receiptText = `
          ==============================
             TRAVEL AGENCY RECEIPT
          ==============================
          Receipt ID: ${receiptData.receiptId}
          Date: ${receiptData.receiptDate}
          Reservation ID: ${receiptData.reservationId}
          Amount Paid: $${receiptData.totalPaid}
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
      .catch(err => {
        alert("Could not generate receipt. Ensure the reservation is PAID/CONFIRMED.");
      });
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING_PAYMENT': return 'warning';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  };

  return (
    <Container className="my-5 animate-fade-up">
      <h2 className="mb-4">My Reservations</h2>
      
      {loading && <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>}
      {error && <Alert variant="danger">{error}</Alert>}

      {!loading && !error && (
        <div className="glass-panel overflow-hidden">
          <Table responsive hover className="mb-0 align-middle">
            <thead className="bg-light">
              <tr>
                <th className="py-3 px-4 border-0">ID</th>
                <th className="py-3 px-4 border-0">Package</th>
                <th className="py-3 px-4 border-0">Passengers</th>
                <th className="py-3 px-4 border-0">Total</th>
                <th className="py-3 px-4 border-0">Status</th>
                <th className="py-3 px-4 border-0 text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map(res => (
                <tr key={res.idReservation}>
                  <td className="px-4 text-muted">#{res.idReservation}</td>
                  <td className="px-4 fw-bold">{res.bundle?.nameBundle || `Package ID: ${res.idBundle}`}</td>
                  <td className="px-4">{res.amountPassengers}</td>
                  <td className="px-4">${res.totalPrice.toLocaleString()}</td>
                  <td className="px-4">
                    <Badge bg={getStatusBadge(res.stateReservation)}>
                      {res.stateReservation}
                    </Badge>
                  </td>
                  <td className="px-4 text-end">
                    {res.stateReservation === 'CONFIRMED' && (
                      <Button 
                        variant="outline-primary" 
                        size="sm"
                        onClick={() => handleDownloadReceipt(res.idReservation)}
                      >
                        Download Receipt
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
              {reservations.length === 0 && (
                <tr>
                  <td colSpan="6" className="text-center py-5 text-muted">You don't have any reservations yet.</td>
                </tr>
              )}
            </tbody>
          </Table>
        </div>
      )}
    </Container>
  );
}

export default MyReservations;

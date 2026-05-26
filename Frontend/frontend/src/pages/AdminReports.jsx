import React, { useState } from 'react';
import { Container, Row, Col, Form, Button, Table, Spinner, Alert, Tabs, Tab } from 'react-bootstrap';
import { reservationService } from '../services/reservationService';

function AdminReports() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const [salesReport, setSalesReport] = useState([]);
  const [rankingReport, setRankingReport] = useState([]);

  const handleGenerateReports = (e) => {
    e.preventDefault();
    if (!startDate || !endDate) {
      setError("Please select both start and end dates.");
      return;
    }
    
    if (new Date(startDate) > new Date(endDate)) {
      setError("Start date cannot be after end date.");
      return;
    }

    setLoading(true);
    setError(null);

    Promise.all([
      reservationService.getSalesReport(startDate, endDate),
      reservationService.getPackageRanking(startDate, endDate)
    ])
      .then(([salesData, rankingData]) => {
        setSalesReport(salesData);
        setRankingReport(rankingData);
        setLoading(false);
      })
      .catch(err => {
        setError("Failed to fetch reports. Please try again.");
        setLoading(false);
      });
  };

  return (
    <Container className="my-5 animate-fade-up">
      <h2 className="mb-4">Admin Reports</h2>

      <div className="glass-panel p-4 mb-4">
        <Form onSubmit={handleGenerateReports}>
          <Row className="align-items-end">
            <Col md={4}>
              <Form.Group>
                <Form.Label>Start Date</Form.Label>
                <Form.Control 
                  type="date" 
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group>
                <Form.Label>End Date</Form.Label>
                <Form.Control 
                  type="date" 
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Button type="submit" className="btn-premium w-100" disabled={loading}>
                {loading ? <Spinner size="sm" /> : 'Generate Reports'}
              </Button>
            </Col>
          </Row>
        </Form>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      {(!loading && !error && (salesReport.length > 0 || rankingReport.length > 0)) && (
        <div className="glass-panel p-4 mt-4">
          <Tabs defaultActiveKey="sales" id="report-tabs" className="mb-4">
            
            {/* SALES REPORT TAB */}
            <Tab eventKey="sales" title="Sales by Period">
              <Table responsive hover className="align-middle">
                <thead className="bg-light">
                  <tr>
                    <th className="py-3">Date</th>
                    <th className="py-3">Client</th>
                    <th className="py-3">Package</th>
                    <th className="py-3">Passengers</th>
                    <th className="py-3">Total Amount</th>
                    <th className="py-3">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {salesReport.map(sale => (
                    <tr key={sale.idReservation}>
                      <td>{new Date(sale.reservationDate).toLocaleDateString()}</td>
                      <td>{sale.clientEmail || `Client ID: ${sale.idClient}`}</td>
                      <td className="fw-bold">{sale.bundle?.nameBundle || sale.idBundle}</td>
                      <td>{sale.amountPassengers}</td>
                      <td>${sale.totalPrice.toLocaleString()}</td>
                      <td>
                        <span className={`badge bg-${sale.stateReservation === 'CONFIRMED' ? 'success' : 'secondary'}`}>
                          {sale.stateReservation}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {salesReport.length === 0 && (
                    <tr><td colSpan="6" className="text-center text-muted py-4">No sales found in this period.</td></tr>
                  )}
                </tbody>
              </Table>
            </Tab>

            {/* RANKING REPORT TAB */}
            <Tab eventKey="ranking" title="Package Ranking">
              <Table responsive hover className="align-middle">
                <thead className="bg-light">
                  <tr>
                    <th className="py-3">Rank</th>
                    <th className="py-3">Package Name</th>
                    <th className="py-3">Total Reservations</th>
                    <th className="py-3">Total Passengers</th>
                    <th className="py-3">Revenue Generated</th>
                  </tr>
                </thead>
                <tbody>
                  {rankingReport.map((item, index) => (
                    <tr key={item.bundleId}>
                      <td className="fw-bold fs-5 text-primary">#{index + 1}</td>
                      <td className="fw-bold">{item.bundleName}</td>
                      <td>{item.totalReservations}</td>
                      <td>{item.totalPassengers}</td>
                      <td>${item.totalRevenue.toLocaleString()}</td>
                    </tr>
                  ))}
                  {rankingReport.length === 0 && (
                    <tr><td colSpan="5" className="text-center text-muted py-4">No packages sold in this period.</td></tr>
                  )}
                </tbody>
              </Table>
            </Tab>

          </Tabs>
        </div>
      )}
    </Container>
  );
}

export default AdminReports;

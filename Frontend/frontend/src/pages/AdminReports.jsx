import React, { useState } from 'react';
import {
  Container, Typography, Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  Chip, Button, CircularProgress, Alert, TextField, Grid, Tabs, Tab
} from '@mui/material';
import AssessmentIcon from '@mui/icons-material/Assessment';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import { reservationService } from '../services/reservationService';

function AdminReports() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [salesReport, setSalesReport] = useState([]);
  const [rankingReport, setRankingReport] = useState([]);
  const [tabValue, setTabValue] = useState(0);

  const handleGenerateReports = (e) => {
    e.preventDefault();
    if (!startDate || !endDate) { setError('Please select both dates.'); return; }
    if (new Date(startDate) > new Date(endDate)) { setError('Start date cannot be after end date.'); return; }

    setLoading(true);
    setError(null);

    Promise.all([
      reservationService.getSalesReport(startDate, endDate),
      reservationService.getPackageRanking(startDate, endDate),
    ])
      .then(([salesData, rankingData]) => {
        setSalesReport(salesData);
        setRankingReport(rankingData);
        setLoading(false);
      })
      .catch(() => { setError('Failed to fetch reports.'); setLoading(false); });
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }} className="animate-fade-up">
      <Typography variant="h4" sx={{ mb: 1 }}>Admin Reports</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Generate sales and ranking reports by period.
      </Typography>

      {/* Date Selector */}
      <Paper sx={{ p: 3, borderRadius: 3, mb: 4 }}>
        <Box component="form" onSubmit={handleGenerateReports}>
          <Grid container spacing={2} alignItems="flex-end">
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField fullWidth type="date" label="Start Date" value={startDate}
                onChange={(e) => setStartDate(e.target.value)} InputLabelProps={{ shrink: true }} required />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField fullWidth type="date" label="End Date" value={endDate}
                onChange={(e) => setEndDate(e.target.value)} InputLabelProps={{ shrink: true }} required />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Button fullWidth variant="contained" type="submit" disabled={loading} size="large" sx={{ height: 40 }}>
                {loading ? <CircularProgress size={20} /> : 'Generate Reports'}
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Paper>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (salesReport.length > 0 || rankingReport.length > 0) && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Tabs value={tabValue} onChange={(_, v) => setTabValue(v)} sx={{ px: 2, pt: 1 }}>
            <Tab icon={<AssessmentIcon />} iconPosition="start" label="Sales by Period" />
            <Tab icon={<EmojiEventsIcon />} iconPosition="start" label="Package Ranking" />
          </Tabs>

          {/* Sales Tab */}
          {tabValue === 0 && (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Client</TableCell>
                  <TableCell>Package</TableCell>
                  <TableCell>Passengers</TableCell>
                  <TableCell>Total Amount</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {salesReport.map(sale => (
                  <TableRow key={sale.id} hover>
                    <TableCell>{new Date(sale.reservationDate).toLocaleDateString()}</TableCell>
                    <TableCell>{sale.user?.email || 'N/A'}</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>{sale.bundle?.nameBundle || 'Unknown'}</TableCell>
                    <TableCell>{sale.numberOfPassengers}</TableCell>
                    <TableCell>${sale.totalAmount?.toLocaleString()}</TableCell>
                    <TableCell>
                      <Chip label={sale.state} size="small"
                        color={sale.state === 'CONFIRMED' ? 'success' : 'default'} sx={{ fontWeight: 700 }} />
                    </TableCell>
                  </TableRow>
                ))}
                {salesReport.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} sx={{ textAlign: 'center', py: 5, color: 'text.secondary' }}>
                      No sales found in this period.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}

          {/* Ranking Tab */}
          {tabValue === 1 && (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Rank</TableCell>
                  <TableCell>Package Name</TableCell>
                  <TableCell>Total Reservations</TableCell>
                  <TableCell>Total Passengers</TableCell>
                  <TableCell>Revenue Generated</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rankingReport.map((item, index) => (
                  <TableRow key={item.bundleId} hover>
                    <TableCell>
                      <Chip label={`#${index + 1}`} size="small" color={index < 3 ? 'primary' : 'default'} sx={{ fontWeight: 700 }} />
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>{item.bundleName}</TableCell>
                    <TableCell>{item.totalReservations}</TableCell>
                    <TableCell>{item.totalPassengers}</TableCell>
                    <TableCell>${item.totalRevenue?.toLocaleString()}</TableCell>
                  </TableRow>
                ))}
                {rankingReport.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} sx={{ textAlign: 'center', py: 5, color: 'text.secondary' }}>
                      No packages sold in this period.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </Paper>
      )}
    </Container>
  );
}

export default AdminReports;

import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  Chip, Button, CircularProgress, Alert, IconButton
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { useNavigate } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import { useAuth } from '../context/AuthProvider';

function AdminDashboard() {
  const [bundles, setBundles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { role } = useAuth();

  useEffect(() => { fetchBundles(); }, []);

  const fetchBundles = () => {
    setLoading(true);
    bundleService.getAllBundles()
      .then(data => { setBundles(data); setLoading(false); })
      .catch(() => { setError('No se pudieron obtener los paquetes.'); setLoading(false); });
  };

  const handleDelete = (id) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar este paquete?')) {
      bundleService.deleteBundle(id)
        .then(() => fetchBundles())
        .catch(() => alert('No se pudo eliminar el paquete.'));
    }
  };

  if (role !== 'ADMIN') {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
        <Paper sx={{ p: 5, borderRadius: 3 }}>
          <Typography variant="h5" color="error" sx={{ mb: 2 }}>Acceso Denegado</Typography>
          <Typography color="text.secondary">No tienes permiso para ver esta página.</Typography>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }} className="animate-fade-up">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4">Panel de Administración</Typography>
          <Typography variant="body2" color="text.secondary">Gestiona tus paquetes de viaje y catálogo.</Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/admin/package/new')}>
          Crear Paquete
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
                <TableCell>Nombre del Paquete</TableCell>
                <TableCell>Destino</TableCell>
                <TableCell>Precio (CLP)</TableCell>
                <TableCell>Cupos</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {bundles.map(bundle => (
                <TableRow key={bundle.idBundle} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{bundle.idBundle}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{bundle.nameBundle}</TableCell>
                  <TableCell sx={{ color: 'text.secondary' }}>{bundle.destinyBundle}</TableCell>
                  <TableCell>${bundle.priceBundle?.toLocaleString()}</TableCell>
                  <TableCell>{bundle.availableSlotsBundle}</TableCell>
                  <TableCell>
                    <Chip label={bundle.stateBundle} size="small"
                      color={bundle.stateBundle === 'AVAILABLE' ? 'success' : 'default'} sx={{ fontWeight: 700 }} />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton size="small" color="primary" onClick={() => navigate(`/admin/package/edit/${bundle.idBundle}`)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => handleDelete(bundle.idBundle)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {bundles.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    No se encontraron paquetes. Crea uno para comenzar.
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

export default AdminDashboard;

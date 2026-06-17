import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  Chip, Button, CircularProgress, Alert
} from '@mui/material';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthProvider';

function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { role } = useAuth();

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = () => {
    setLoading(true);
    userService.getAllUsers()
      .then(data => { setUsers(data); setLoading(false); })
      .catch(() => { setError('No se pudieron obtener los usuarios.'); setLoading(false); });
  };

  const handleToggleActive = (id) => {
    if (window.confirm('¿Cambiar el estado del usuario?')) {
      userService.toggleUserActive(id)
        .then(() => fetchUsers())
        .catch(() => alert('No se pudo actualizar el estado del usuario.'));
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
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4">Gestión de Usuarios</Typography>
        <Typography variant="body2" color="text.secondary">Gestiona todos los usuarios registrados en la plataforma.</Typography>
      </Box>

      {loading && <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Teléfono</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Rol</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map(user => (
                <TableRow key={user.id} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>#{user.id}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{user.fullName || 'N/A'}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.phone || 'N/A'}</TableCell>
                  <TableCell>
                    <Chip label={user.active ? 'ACTIVO' : 'INACTIVO'} size="small"
                      color={user.active ? 'success' : 'error'} sx={{ fontWeight: 700 }} />
                  </TableCell>
                  <TableCell>
                    <Chip label={user.role} size="small" variant="outlined"
                      color={user.role === 'ADMIN' ? 'primary' : 'default'} sx={{ fontWeight: 700 }} />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" variant="outlined"
                      color={user.active ? 'error' : 'success'}
                      onClick={() => handleToggleActive(user.id)}
                    >
                      {user.active ? 'Desactivar' : 'Activar'}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    No se encontraron usuarios.
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

export default AdminUsers;

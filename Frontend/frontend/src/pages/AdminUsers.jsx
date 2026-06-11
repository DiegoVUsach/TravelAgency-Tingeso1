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
      .catch(() => { setError('Failed to fetch users.'); setLoading(false); });
  };

  const handleToggleActive = (id) => {
    if (window.confirm('Toggle user status?')) {
      userService.toggleUserActive(id)
        .then(() => fetchUsers())
        .catch(() => alert('Failed to update user status.'));
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
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4">User Management</Typography>
        <Typography variant="body2" color="text.secondary">Manage all registered users in the platform.</Typography>
      </Box>

      {loading && <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!loading && !error && (
        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Phone</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Role</TableCell>
                <TableCell align="right">Actions</TableCell>
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
                    <Chip label={user.active ? 'ACTIVE' : 'INACTIVE'} size="small"
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
                      {user.active ? 'Deactivate' : 'Activate'}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                    No users found.
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

import React, { useState } from 'react';
import {
  AppBar, Toolbar, Typography, Button, Box, IconButton, Menu, MenuItem,
  Avatar, Divider, ListItemIcon, Container
} from '@mui/material';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import PersonIcon from '@mui/icons-material/Person';
import LogoutIcon from '@mui/icons-material/Logout';
import BookOnlineIcon from '@mui/icons-material/BookOnline';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import ExploreIcon from '@mui/icons-material/Explore';
import LoginIcon from '@mui/icons-material/Login';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthProvider';

function Navbar() {
  const { isAuthenticated, user, localUser, hasRole, login, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleMenu = (e) => setAnchorEl(e.currentTarget);
  const handleClose = () => setAnchorEl(null);

  const displayName = localUser?.fullName || user?.firstName || user?.username || 'Usuario';
  const initials = displayName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();

  return (
    <AppBar position="sticky">
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ gap: 1 }}>
          {/* Logo */}
          <FlightTakeoffIcon sx={{ color: 'primary.main', mr: 1 }} />
          <Typography
            variant="h6"
            component={Link}
            to="/"
            sx={{
              fontWeight: 800, color: 'white', textDecoration: 'none', mr: 4,
              background: 'linear-gradient(135deg, #aa3bff, #00bcd4)',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
            }}
          >
            TravelAgency
          </Typography>

          {/* Nav Links */}
          <Box sx={{ display: 'flex', gap: 0.5, flexGrow: 1 }}>
            <Button
              component={Link} to="/catalog"
              startIcon={<ExploreIcon />}
              sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}
            >
              Catálogo
            </Button>

            {isAuthenticated && hasRole('USER') && (
              <Button
                component={Link} to="/my-reservations"
                startIcon={<BookOnlineIcon />}
                sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}
              >
                Mis Reservas
              </Button>
            )}

            {isAuthenticated && hasRole('ADMIN') && (
              <>
                <Button component={Link} to="/admin/dashboard" startIcon={<DashboardIcon />}
                  sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}>
                  Paquetes
                </Button>
                <Button component={Link} to="/admin/reservations" startIcon={<ReceiptLongIcon />}
                  sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}>
                  Reservas
                </Button>
                <Button component={Link} to="/admin/users" startIcon={<PeopleIcon />}
                  sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}>
                  Usuarios
                </Button>
                <Button component={Link} to="/admin/reports" startIcon={<AssessmentIcon />}
                  sx={{ color: 'text.secondary', '&:hover': { color: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}>
                  Reportes
                </Button>
              </>
            )}
          </Box>

          {/* User Section */}
          {isAuthenticated ? (
            <>
              <Button
                onClick={handleMenu}
                sx={{
                  color: 'white', textTransform: 'none', gap: 1,
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: 3, px: 2,
                  '&:hover': { borderColor: 'primary.main', bgcolor: 'rgba(170,59,255,0.1)' },
                }}
              >
                <Avatar sx={{ width: 28, height: 28, bgcolor: 'primary.main', fontSize: '0.75rem', fontWeight: 700 }}>
                  {initials}
                </Avatar>
                <Typography variant="body2" fontWeight={600}>{displayName}</Typography>
              </Button>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
                PaperProps={{
                  sx: { mt: 1.5, minWidth: 200, bgcolor: '#1a1a24', border: '1px solid rgba(255,255,255,0.08)' }
                }}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              >
                <MenuItem onClick={() => { handleClose(); navigate('/profile'); }}>
                  <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
                  Mi Perfil
                </MenuItem>
                <MenuItem onClick={() => { handleClose(); navigate('/my-reservations'); }}>
                  <ListItemIcon><BookOnlineIcon fontSize="small" /></ListItemIcon>
                  Mis Reservas
                </MenuItem>
                <Divider />
                <MenuItem onClick={() => { handleClose(); logout(); navigate('/'); }}>
                  <ListItemIcon><LogoutIcon fontSize="small" color="error" /></ListItemIcon>
                  <Typography color="error.main">Cerrar Sesión</Typography>
                </MenuItem>
              </Menu>
            </>
          ) : (
            <Button variant="contained" startIcon={<LoginIcon />} onClick={login} size="small">
              Iniciar Sesión
            </Button>
          )}
        </Toolbar>
      </Container>
    </AppBar>
  );
}

export default Navbar;
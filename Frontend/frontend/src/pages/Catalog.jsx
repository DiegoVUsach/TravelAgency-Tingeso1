import React, { useEffect, useState } from 'react';
import {
  Box, Container, Typography, TextField, Grid, MenuItem, Button,
  CircularProgress, Alert, InputAdornment, Chip, Paper, Select, FormControl, InputLabel
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import TuneIcon from '@mui/icons-material/Tune';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import { useSearchParams } from 'react-router-dom';
import { bundleService } from '../services/bundleService';
import BundleCard from '../components/BundleCard';

function Catalog() {
  const [searchParams] = useSearchParams();
  const [bundles, setBundles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [filters, setFilters] = useState({
    destiny: searchParams.get('search') || '',
    minPrice: '',
    maxPrice: '',
    duration: '',
    startDate: '',
    endDate: '',
    experience: searchParams.get('experience') || ''
  });

  useEffect(() => {
    handleSearch();
  }, []);

  // If URL params change (e.g. from Landing search), update filters and search
  useEffect(() => {
    const search = searchParams.get('search') || '';
    const experience = searchParams.get('experience') || '';
    if (search || experience) {
      const newFilters = { ...filters, destiny: search, experience };
      setFilters(newFilters);
      setLoading(true);
      bundleService.searchAvailableBundles(newFilters)
        .then(data => { setBundles(data); setLoading(false); })
        .catch(() => { setError('Could not load catalog.'); setLoading(false); });
    }
  }, [searchParams]);

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    bundleService.searchAvailableBundles(filters)
      .then(data => { setBundles(data); setLoading(false); })
      .catch(() => { setError('Could not load catalog.'); setLoading(false); });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFilters({ ...filters, [name]: value });
  };

  const handleReset = () => {
    setFilters({ destiny: '', minPrice: '', maxPrice: '', duration: '', startDate: '', endDate: '', experience: '' });
    setLoading(true);
    bundleService.searchAvailableBundles({})
      .then(data => { setBundles(data); setLoading(false); })
      .catch(() => { setError('Could not load catalog.'); setLoading(false); });
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Grid container spacing={3}>
        {/* SIDEBAR FILTERS */}
        <Grid size={{ xs: 12, md: 3 }}>
          <Paper sx={{ p: 3, position: 'sticky', top: 80, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
              <TuneIcon color="primary" /> Filters
            </Typography>

            <Box component="form" onSubmit={handleSearch}>
              <TextField
                fullWidth label="Destination" name="destiny"
                value={filters.destiny} onChange={handleInputChange}
                sx={{ mb: 2 }}
                InputProps={{
                  startAdornment: <InputAdornment position="start"><SearchIcon sx={{ fontSize: 18, color: 'text.secondary' }} /></InputAdornment>
                }}
              />

              <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>
                Price Range (CLP)
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <TextField name="minPrice" type="number" placeholder="Min" value={filters.minPrice} onChange={handleInputChange} size="small" />
                <TextField name="maxPrice" type="number" placeholder="Max" value={filters.maxPrice} onChange={handleInputChange} size="small" />
              </Box>

              <TextField
                fullWidth label="Duration (Days)" name="duration" type="number"
                value={filters.duration} onChange={handleInputChange}
                sx={{ mb: 2 }}
              />

              <TextField
                fullWidth label="Start Date (After)" name="startDate" type="date"
                value={filters.startDate} onChange={handleInputChange}
                sx={{ mb: 2 }}
                InputLabelProps={{ shrink: true }}
              />

              <FormControl fullWidth sx={{ mb: 3 }}>
                <InputLabel>Experience Type</InputLabel>
                <Select
                  name="experience" value={filters.experience}
                  onChange={handleInputChange} label="Experience Type"
                >
                  <MenuItem value="">Any Experience</MenuItem>
                  <MenuItem value="RELAX">Relax</MenuItem>
                  <MenuItem value="ADVENTURE">Adventure</MenuItem>
                  <MenuItem value="CULTURAL">Cultural</MenuItem>
                  <MenuItem value="FAMILY">Family</MenuItem>
                  <MenuItem value="ROMANTIC">Romantic</MenuItem>
                  <MenuItem value="BUSINESS">Business</MenuItem>
                </Select>
              </FormControl>

              <Button fullWidth variant="contained" type="submit" sx={{ mb: 1 }}>
                Apply Filters
              </Button>
              <Button fullWidth variant="outlined" startIcon={<RestartAltIcon />} onClick={handleReset}>
                Reset
              </Button>
            </Box>
          </Paper>
        </Grid>

        {/* RESULTS */}
        <Grid size={{ xs: 12, md: 9 }}>
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box>
              <Typography variant="h4">Available Packages</Typography>
              <Typography variant="body2" color="text.secondary">
                {!loading && `${bundles.length} package${bundles.length !== 1 ? 's' : ''} found`}
              </Typography>
            </Box>
            {filters.experience && (
              <Chip
                label={`Experience: ${filters.experience}`}
                color="primary"
                variant="outlined"
                onDelete={() => { setFilters({...filters, experience: ''}); }}
              />
            )}
          </Box>

          {loading && (
            <Box sx={{ textAlign: 'center', py: 8 }}>
              <CircularProgress />
            </Box>
          )}
          {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

          <Grid container spacing={3} className="stagger-children">
            {bundles.map(bundle => (
              <Grid size={{ xs: 12, sm: 6, lg: 4 }} key={bundle.idBundle}>
                <BundleCard bundle={bundle} />
              </Grid>
            ))}
          </Grid>

          {!loading && bundles.length === 0 && (
            <Paper sx={{ textAlign: 'center', py: 8, borderRadius: 3 }}>
              <Typography variant="h6" color="text.secondary" sx={{ mb: 1 }}>No results found</Typography>
              <Typography variant="body2" color="text.secondary">Try adjusting your filters or search criteria.</Typography>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Container>
  );
}

export default Catalog;

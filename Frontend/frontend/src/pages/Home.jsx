import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Form, Button, Card, Spinner, Alert } from 'react-bootstrap';
import { bundleService } from '../services/bundleService';
import BundleCard from '../components/BundleCard';

function Home() {
  const [bundles, setBundles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // One object to manage all filter states
  const [filters, setFilters] = useState({
    destiny: '',
    minPrice: '',
    maxPrice: '',
    duration: '',
    startDate: '',
    endDate: '',
    experience: '' 
  });

  useEffect(() => {
    handleSearch();
  }, []);

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    bundleService.searchAvailableBundles(filters)
      .then(data => {
        setBundles(data);
        setLoading(false);
      })
      .catch(() => {
        setError("Could not load catalog.");
        setLoading(false);
      });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFilters({ ...filters, [name]: value });
  };

  return (
    <Container fluid> {/* Use fluid for full width */}
      <Row>
        {/* SIDEBAR COLUMN */}
        <Col md={3} className="bg-light p-4 shadow-sm" style={{ minHeight: '90vh' }}>
          <h4>Filters</h4>
          <Form onSubmit={handleSearch}>
            <Form.Group className="mb-3">
              <Form.Label>Destiny</Form.Label>
              <Form.Control name="destiny" value={filters.destiny} onChange={handleInputChange} placeholder="e.g. Patagonia" />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Price Range (Min - Max)</Form.Label>
              <div className="d-flex gap-2">
                <Form.Control name="minPrice" type="number" value={filters.minPrice} onChange={handleInputChange} placeholder="Min" />
                <Form.Control name="maxPrice" type="number" value={filters.maxPrice} onChange={handleInputChange} placeholder="Max" />
              </div>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Duration (Days)</Form.Label>
              <Form.Control name="duration" type="number" value={filters.duration} onChange={handleInputChange} />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Start Date (After)</Form.Label>
              <Form.Control name="startDate" type="date" value={filters.startDate} onChange={handleInputChange} />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Experience Type</Form.Label>
              <Form.Select name="experience" value={filters.experience} onChange={handleInputChange}>
                <option value="">Any Experience (All)</option>
                <option value="RELAX">Relax</option>
                <option value="ADVENTURE">Adventure</option>
                <option value="CULTURAL">Cultural</option>
                <option value="FAMILY">Family</option>
                <option value="ROMANTIC">Romantic</option>
                <option value="BUSINESS">Business</option>
                <option value="NATURE">Nature</option>
                <option value="CULINARY">Culinary</option>
                <option value="WELLNESS">Wellness</option>
                <option value="NIGHTLIFE">Nightlife</option>
              </Form.Select>
            </Form.Group>

            <Button variant="primary" type="submit" className="w-100 mt-3">
              Apply Filters
            </Button>
            <Button variant="outline-secondary" className="w-100 mt-2" onClick={() => window.location.reload()}>
              Reset
            </Button>
          </Form>
        </Col>

        {/* RESULTS COLUMN */}
        <Col md={9} className="p-4">
          <h2 className="mb-4">Available Packages</h2>
          
          {loading && <Spinner animation="border" variant="primary" />}
          {error && <Alert variant="danger">{error}</Alert>}
          
          <Row xs={1} lg={2} xl={3} className="g-4">
            {bundles.map(bundle => (
              <Col key={bundle.idBundle}>
                <BundleCard bundle={bundle} />
              </Col>
            ))}
          </Row>

          {!loading && bundles.length === 0 && (
            <Alert variant="info" className="mt-4">No results found matching your criteria.</Alert>
          )}
        </Col>
      </Row>
    </Container>
  );
}

export default Home;
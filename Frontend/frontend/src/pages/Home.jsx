import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Spinner, Alert, Form, Button, Card } from 'react-bootstrap';
import { bundleService } from '../services/bundleService';
import BundleCard from '../components/BundleCard';

function Home() {
  const [bundles, setBundles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Estados para los filtros de búsqueda
  const [searchDestiny, setSearchDestiny] = useState('');
  const [searchMaxPrice, setSearchMaxPrice] = useState('');

  // Cargar todos los paquetes al inicio
  useEffect(() => {
    fetchBundles();
  }, []);

  const fetchBundles = () => {
    setLoading(true);
    bundleService.getAllBundles()
      .then((data) => {
        setBundles(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError("Error fetching the catalog.");
        setLoading(false);
      });
  };

  // Función que se ejecuta al presionar "Search"
  const handleSearch = (e) => {
    e.preventDefault(); // Evita que la página se recargue
    setLoading(true);
    
    bundleService.searchBundles(searchDestiny, searchMaxPrice)
      .then((data) => {
        setBundles(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError("Error searching packages.");
        setLoading(false);
      });
  };

  // Función para limpiar filtros
  const handleClear = () => {
    setSearchDestiny('');
    setSearchMaxPrice('');
    fetchBundles(); // Vuelve a traer todo
  };

  return (
    <Container>
      <h2 className="mb-4">Explore our Travel Packages</h2>

      {/* --- SECCIÓN DEL BUSCADOR --- */}
      <Card className="mb-4 shadow-sm">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="align-items-end">
              <Col md={4} className="mb-3 mb-md-0">
                <Form.Group>
                  <Form.Label>Destiny</Form.Label>
                  <Form.Control 
                    type="text" 
                    placeholder="e.g. Patagonia" 
                    value={searchDestiny}
                    onChange={(e) => setSearchDestiny(e.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col md={4} className="mb-3 mb-md-0">
                <Form.Group>
                  <Form.Label>Max Price (CLP)</Form.Label>
                  <Form.Control 
                    type="number" 
                    placeholder="e.g. 500000" 
                    value={searchMaxPrice}
                    onChange={(e) => setSearchMaxPrice(e.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Button variant="primary" type="submit" className="me-2">
                  Search
                </Button>
                <Button variant="secondary" onClick={handleClear}>
                  Clear
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      {/* --- FIN DEL BUSCADOR --- */}
      
      {loading && <Spinner animation="border" variant="primary" />}
      {error && <Alert variant="danger">{error}</Alert>}
      
      {!loading && !error && (
        <Row xs={1} md={2} lg={3} className="g-4">
          {bundles.map((bundle) => (
            <Col key={bundle.idBundle}>
              <BundleCard bundle={bundle} />
            </Col>
          ))}
        </Row>
      )}
      
      {!loading && bundles.length === 0 && !error && (
        <Alert variant="info" className="mt-3">No packages found for these filters.</Alert>
      )}
    </Container>
  );
}

export default Home;
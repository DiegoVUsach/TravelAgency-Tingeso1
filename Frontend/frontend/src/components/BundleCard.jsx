import React from 'react';
import { Card, Button, Badge } from 'react-bootstrap';

function BundleCard({ bundle }) {
  return (
    <Card className="h-100 shadow-sm">
      <Card.Body>
        <Card.Title>{bundle.nameBundle}</Card.Title>
        <Card.Subtitle className="mb-2 text-muted">
          📍 {bundle.destinyBundle}
        </Card.Subtitle>
        <Card.Text>
          {bundle.descBundle}
        </Card.Text>
        <div className="d-flex justify-content-between align-items-center mt-3">
          <h5 className="text-primary mb-0">${bundle.priceBundle.toLocaleString()} CLP</h5>
          <Badge bg={bundle.stateBundle === 'AVAILABLE' ? 'success' : 'danger'}>
            {bundle.stateBundle}
          </Badge>
        </div>
      </Card.Body>
      <Card.Footer className="bg-white border-top-0">
        <Button variant="outline-primary" className="w-100" onClick={() => window.location.href=`/package/${bundle.idBundle}`}>
          View Details / Book
        </Button>
      </Card.Footer>
    </Card>
  );
}

export default BundleCard;
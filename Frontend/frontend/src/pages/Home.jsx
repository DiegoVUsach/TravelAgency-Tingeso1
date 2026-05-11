import React, { useEffect, useState } from 'react';
import { bundleService } from '../services/bundleService';

function Home() {
  const [bundles, setBundles] = useState([]); // empty array for storing bundles from backend

  // useEffect runs the code inside exactly once when the page loads, and we use it to fetch the bundles from the backend
  useEffect(() => {
    bundleService.getAllBundles()
      .then((data) => {
        console.log("Success! Data from backend:", data);
        setBundles(data); // We save the data in our state
      })
      .catch((error) => {
        console.error("Oops! Connection failed:", error);
      });
  }, []);

  return (
    <div>
      <h1>Welcome to TravelAgency</h1>
      <p>Packages currently in database: <strong>{bundles.length}</strong></p>
      
      {/* If we have bundles, we can map through them to show their names */}
      <ul>
        {bundles.map((bundle) => (
          <li key={bundle.idBundle}>{bundle.nameBundle} - ${bundle.priceBundle}</li>
        ))}
      </ul>
    </div>
  );
}

export default Home;
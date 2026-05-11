import api from './api';

export const bundleService = {
    // Fetches all available bundles from the backend
    getAllBundles: async () => {
        try {
            const response = await api.get('/bundle'); //bundle endpoint
            return response.data;
        } catch (error) {
            console.error("Error fetching bundles:", error);
            throw error;
        }
    }
};
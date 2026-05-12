import api from './api';

export const bundleService = {
    getAllBundles: async () => {
        try {
            const response = await api.get('/bundle');
            return response.data;
        } catch (error) {
            console.error("Error fetching bundles:", error);
            throw error;
        }
    },

    searchAvailableBundles: async (filters) => {
        try {
            const response = await api.get('/bundle/search', {
                params: {
                    destiny: filters.destiny || null,
                    minPrice: filters.minPrice || null,
                    maxPrice: filters.maxPrice || null,
                    duration: filters.duration || null,
                    startDate: filters.startDate || null,
                    endDate: filters.endDate || null,
                    experience: filters.experience || null 
                }
            });
            return response.data;
        } catch (error) {
            console.error("Error searching bundles:", error);
            throw error;
        }
    }
};
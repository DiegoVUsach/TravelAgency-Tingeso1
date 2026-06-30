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

    getBundleById: async (id) => {
        try {
            const response = await api.get(`/bundle/${id}`);
            return response.data;
        } catch (error) {
            console.error("Error fetching bundle:", error);
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
                    experience: filters.experience || null,
                    season: filters.season || null,
                    category: filters.category || null
                }
            });
            return response.data;
        } catch (error) {
            console.error("Error searching bundles:", error);
            throw error;
        }
    },

    createBundle: async (bundleData) => {
        try {
            const response = await api.post('/bundle', bundleData);
            return response.data;
        } catch (error) {
            console.error("Error creating bundle:", error);
            throw error;
        }
    },

    updateBundle: async (id, bundleData) => {
        try {
            const response = await api.put(`/bundle/${id}`, bundleData);
            return response.data;
        } catch (error) {
            console.error("Error updating bundle:", error);
            throw error;
        }
    },

    deleteBundle: async (id) => {
        try {
            const response = await api.delete(`/bundle/${id}`);
            return response.data;
        } catch (error) {
            console.error("Error deleting bundle:", error);
            throw error;
        }
    }
};
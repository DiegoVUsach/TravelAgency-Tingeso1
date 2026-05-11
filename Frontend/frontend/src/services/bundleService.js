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

    // NUEVA FUNCIÓN: Envía los parámetros a tu backend
    searchBundles: async (destiny, maxPrice) => {
        try {
            // Axios arma automáticamente la URL: /bundle/search?destiny=X&maxPrice=Y
            const response = await api.get('/bundle/search', {
                params: {
                    destiny: destiny || null, // Si está vacío, envía null
                    maxPrice: maxPrice || null
                }
            });
            return response.data;
        } catch (error) {
            console.error("Error searching bundles:", error);
            throw error;
        }
    }
};
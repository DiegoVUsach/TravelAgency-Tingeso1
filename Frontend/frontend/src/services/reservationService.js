import api from './api';

export const reservationService = {
    createReservation: async (reservationData) => {
        try {
            const response = await api.post('/reservation', reservationData);
            return response.data;
        } catch (error) {
            console.error("Error creating reservation:", error);
            throw error;
        }
    },
    
    // Future epics might use these
    getClientReservations: async (clientId) => {
        try {
            const response = await api.get(`/reservation/client/${clientId}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching reservations for client ${clientId}:`, error);
            throw error;
        }
    }
};

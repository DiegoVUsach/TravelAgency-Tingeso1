import api from './api';

export const reservationService = {
    // E4: Create Reservation (Cart)
    quoteReservation: async (reservationData) => {
        try {
            const response = await api.post('/reservations/quote', reservationData);
            return response.data;
        } catch (error) {
            console.error("Error quoting reservation:", error);
            throw error;
        }
    },

    createReservation: async (reservationData) => {
        try {
            // Note: backend endpoint is /reservations/cart expecting ReservationRequestDTO
            const response = await api.post('/reservations/cart', reservationData);
            return response.data;
        } catch (error) {
            console.error("Error creating reservation:", error);
            throw error;
        }
    },
    
    // E6: Get My Reservations
    getMyReservations: async () => {
        try {
            const response = await api.get('/reservations/my-reservations');
            return response.data;
        } catch (error) {
            console.error("Error fetching my reservations:", error);
            throw error;
        }
    },

    // E6: Get Receipt
    getReceipt: async (id) => {
        try {
            const response = await api.get(`/reservations/${id}/receipt`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching receipt for ${id}:`, error);
            throw error;
        }
    },

    // Admin: Get all reservations
    getAllReservations: async () => {
        try {
            const response = await api.get('/reservations/all');
            return response.data;
        } catch (error) {
            console.error("Error fetching all reservations:", error);
            throw error;
        }
    },

    // Admin: Update state
    updateReservationState: async (id, state) => {
        try {
            const response = await api.patch(`/reservations/${id}/state`, null, {
                params: { newState: state }
            });
            return response.data;
        } catch (error) {
            console.error(`Error updating reservation state for ${id}:`, error);
            throw error;
        }
    },

    // E7: Sales Report
    getSalesReport: async (startDate, endDate) => {
        try {
            const response = await api.get('/reservations/reports/sales', {
                params: { startDate, endDate }
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching sales report:", error);
            throw error;
        }
    },

    // E7: Package Ranking Report
    getPackageRanking: async (startDate, endDate) => {
        try {
            const response = await api.get('/reservations/reports/ranking', {
                params: { startDate, endDate }
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching package ranking:", error);
            throw error;
        }
    }
};

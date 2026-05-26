import api from './api';

export const paymentService = {
    // E5: Process Payment
    processPayment: async (paymentData) => {
        try {
            // paymentData should contain: reservationId, amount, paymentMethod
            const response = await api.post('/payments', paymentData);
            return response.data;
        } catch (error) {
            console.error("Error processing payment:", error);
            throw error;
        }
    }
};

import api from './api';

export const userService = {
    syncUser: async () => {
        const response = await api.post('/users/sync');
        return response.data;
    },

    getMyProfile: async () => {
        const response = await api.get('/users/me');
        return response.data;
    },

    updateMyProfile: async (data) => {
        const response = await api.put('/users/me', data);
        return response.data;
    },

    deleteMyAccount: async () => {
        const response = await api.delete('/users/me');
        return response.data;
    },

    // Admin operations
    getAllUsers: async () => {
        const response = await api.get('/users');
        return response.data;
    },

    getUserById: async (id) => {
        const response = await api.get(`/users/${id}`);
        return response.data;
    },

    toggleUserActive: async (id) => {
        const response = await api.put(`/users/${id}/toggle-active`);
        return response.data;
    }
};

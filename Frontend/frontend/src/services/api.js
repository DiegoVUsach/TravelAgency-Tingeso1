import axios from 'axios';

//  configure the base connection to  Spring Boot backend
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1', 
    headers: {
        'Content-Type': 'application/json'
    }
});

import keycloak from './keycloak';

// Interceptor to add Keycloak token dynamically
api.interceptors.request.use(
    (config) => {
        if (keycloak.token) {
            config.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
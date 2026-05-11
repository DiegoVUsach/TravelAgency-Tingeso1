import axios from 'axios';

//  configure the base connection to  Spring Boot backend
const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1', 
    headers: {
        'Content-Type': 'application/json'
    }
});

export default api;
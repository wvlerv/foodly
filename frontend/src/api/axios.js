import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  config.headers.Authorization = `Bearer MOCK_TOKEN`;
  return config;
});

export default api;

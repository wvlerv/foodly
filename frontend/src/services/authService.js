import api from '../api/axios';

const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  },

  register: async (email, password) => {
    return await api.post('/auth/register', { email, password });
  },

  logout: () => {
    localStorage.removeItem('token');
  },

  getCurrentToken: () => {
    return localStorage.getItem('token');
  },
};

export default authService;

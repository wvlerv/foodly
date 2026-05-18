import api from '../api/axios';

const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  },

  register: async ({ firstName, lastName, username, email, password }) => {
    return await api.post('/auth/register', { firstName, lastName, username, email, password });
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
      console.log('Successfully logged out from server-side blacklist.');
    } catch (error) {
      console.error('Server-side logout failed:', error.response?.data || error.message);
    } finally {
      localStorage.removeItem('token');
    }
  },

  getCurrentToken: () => {
    return localStorage.getItem('token');
  },
};

export default authService;

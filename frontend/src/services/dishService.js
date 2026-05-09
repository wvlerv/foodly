import axios from 'axios';

/**
 * API Service - Handles all backend communication
 * Integrates with the Spring Boot backend at http://localhost:8080/api
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const normalizeDish = (dish) => ({
  ...dish,
  available: dish.available ?? dish.isAvailable ?? true,
  isAvailable: dish.isAvailable ?? dish.available ?? true,
});

// Add authorization header to all requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken') || 'MOCK_TOKEN';
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

/**
 * Get all available dishes or filtered dishes
 * @param {number} remainingKcal - Optional: filter by "Fit my day" calories
 * @param {string} sortBy - Optional: sort by "proteins" or "calories"
 * @returns {Promise<Array>} Array of dish objects
 */
export const getAllDishes = async (remainingKcal = null, sortBy = null, sortDir = null) => {
  try {
    const params = {};
    if (remainingKcal !== null) {
      params.remainingKcal = remainingKcal;
    }
    if (sortBy) {
      params.sortBy = sortBy;
    }
    if (sortDir) {
      params.sortDir = sortDir;
    }

    const response = await apiClient.get('/dishes', { params });
    return Array.isArray(response.data) ? response.data.map(normalizeDish) : [];
  } catch (error) {
    console.error('Error fetching dishes:', error);
    throw error;
  }
};

/**
 * Apply "Fit my day" filter with specific calorie limit
 * @param {number} remainingKcal - Remaining calories for the day
 * @param {string} sortBy - Optional: sort by "proteins" or "calories"
 * @returns {Promise<Array>} Array of filtered dish objects
 */
export const fitMyDay = async (remainingKcal, sortBy = null, sortDir = null) => {
  return getAllDishes(remainingKcal, sortBy, sortDir);
};

export default apiClient;

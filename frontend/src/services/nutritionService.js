import api from '../api/axios';

/**
 * Отримує дані про споживання калорій для графіка
 */
export const getNutritionLogs = async () => {
  try {
    const response = await api.get('/nutrition/logs');
    return response;
  } catch (error) {
    console.error('Error fetching nutrition logs:', error);
    throw error; // Передаємо помилку далі, щоб компонент міг використати fallback-дані
  }
};

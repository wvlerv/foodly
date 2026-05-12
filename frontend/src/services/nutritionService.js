import axios from 'axios';

const API_URL = 'http://localhost:8080/api/nutrition/logs';

export const getNutritionLogs = () => {
  return axios.get(API_URL);
};

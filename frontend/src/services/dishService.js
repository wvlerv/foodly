import api from '../api/axios';

export const getAllDishes = async (remainingKcal = null, sortBy = null) => {
  try {
    const params = {};
    if (remainingKcal !== null) params.remainingKcal = remainingKcal;
    if (sortBy) params.sortBy = sortBy;

    const response = await api.get('/dishes', { params });
    const normalizeDish = (dish) => ({
      ...dish,
      available: dish.available ?? dish.isAvailable ?? true,
      isAvailable: dish.isAvailable ?? dish.available ?? true,
    });

    return Array.isArray(response.data) ? response.data.map(normalizeDish) : [];
  } catch (error) {
    console.error('Error fetching dishes:', error);
    throw error;
  }
};

export const toggleDishAvailability = async (dishId, isAvailable) => {
  try {
    const response = await api.put(
      `/dishes/${dishId}/toggle-availability?available=${isAvailable}`
    );
    return response.data;
  } catch (error) {
    console.error('Error toggling dish availability:', error);
    throw error;
  }
};

export const fitMyDay = async (remainingKcal, sortBy = null) => {
  return getAllDishes(remainingKcal, sortBy);
};

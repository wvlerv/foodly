// Функція округлення (NFR-05)
export const formatCalories = (value) => {
  return Math.round(value).toString();
};

export const getCaloriesTotal = (items = []) => {
  return items.reduce(
    (sum, item) => sum + Number(item?.calories || 0) * Number(item?.quantity || 1),
    0
  );
};

export const calculateRemainingCalories = ({
  dailyCalorieIntake = 0,
  deliveredCalories = 0,
  cartCalories = 0,
}) => {
  const remaining =
    Number(dailyCalorieIntake || 0) - Number(deliveredCalories || 0) - Number(cartCalories || 0);

  return Number.isFinite(remaining) ? Math.round(remaining) : 0;
};

// Логіка фільтрації "Fit my day" (FR-04)
export const filterMealsByLimit = (meals, limit) => {
  const numericLimit = Number(limit);

  if (!Number.isFinite(numericLimit)) {
    return meals;
  }

  return meals.filter((meal) => Number(meal?.calories || 0) <= numericLimit);
};

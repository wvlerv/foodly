// Функція округлення (NFR-05)
export const formatCalories = (value) => {
    return Math.round(value).toString();
};

// Логіка фільтрації "Fit my day" (FR-04)
export const filterMealsByLimit = (meals, limit) => {
    return meals.filter(meal => meal.calories <= limit);
};
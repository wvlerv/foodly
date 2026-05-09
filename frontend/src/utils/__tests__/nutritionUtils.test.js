import { formatCalories, filterMealsByLimit } from '../nutritionUtils';

describe('Nutrition Utilities', () => {
  test('Тест №4: Округлення калорій за правилами Math.round', () => {
    expect(formatCalories(149.99)).toBe('150');
    expect(formatCalories(149.4)).toBe('149');
  });

  test('Тест №5: Фільтрація страв "Fit my day"', () => {
    const meals = [
      { name: 'Salad', calories: 200 },
      { name: 'Steak', calories: 700 },
    ];
    const limit = 500;

    const result = filterMealsByLimit(meals, limit);

    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('Salad');
  });
});

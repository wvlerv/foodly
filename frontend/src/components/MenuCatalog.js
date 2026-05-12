import api from '../api/axios';
import React, { useState, useEffect } from 'react';
import { UtensilsCrossed } from 'lucide-react';
import DishCard from './DishCard';
import { getAllDishes } from '../services/dishService';
import './MenuCatalog.css';

const MenuCatalog = ({ dishes: mockDishes, onAddToCart }) => {
  const [dishes, setDishes] = useState([]);
  const [useFitMyDay, setUseFitMyDay] = useState(false);
  const [remainingKcal, setRemainingKcal] = useState('500');
  const [sortOption, setSortOption] = useState('none');

  const [selectedAllergens, setSelectedAllergens] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [availableAllergens, setAvailableAllergens] = useState([]);

  const mapSortOptionToParams = (option) => {
    switch (option) {
      case 'proteins':
        return { sortBy: 'proteins' };
      case 'calories':
        return { sortBy: 'calories' };
      case 'fats':
        return { sortBy: 'fats' };
      case 'carbs':
        return { sortBy: 'carbs' };
      default:
        return { sortBy: null };
    }
  };

  const collectAllergens = (dishList) => {
    const values = new Set();
    dishList.forEach((dish) => {
      if (Array.isArray(dish.allergens)) {
        dish.allergens.forEach((allergen) => values.add(allergen));
      }
    });
    return Array.from(values).sort();
  };

  useEffect(() => {
    const fetchDishes = async () => {
      try {
        setLoading(true);

        const parsedKcal = remainingKcal === '' ? null : Number(remainingKcal);
        const appliedKcal = useFitMyDay ? parsedKcal : null;
        const { sortBy } = mapSortOptionToParams(sortOption);

        const data = await getAllDishes(appliedKcal, sortBy);

        setDishes(data);
        setAvailableAllergens(collectAllergens(data));
        setError(null);
      } catch (err) {
        console.error('Помилка інтеграції:', err);
        setDishes(mockDishes || []);
        setError('Could not load fresh menu. Showing offline version.');
      } finally {
        setLoading(false);
      }
    };
    fetchDishes();
  }, [useFitMyDay, remainingKcal, sortOption, mockDishes]);

  // Логіка перемикання алергенів (додати/видалити з масиву)
  const toggleAllergen = (allergen) => {
    setSelectedAllergens((prev) =>
      prev.includes(allergen) ? prev.filter((a) => a !== allergen) : [...prev, allergen]
    );
  };

  // Фільтрація: страва зникає, якщо в ній є ХОЧА Б ОДИН із обраних алергенів
  const visibleDishes = dishes.filter((dish) => {
    if (selectedAllergens.length === 0) return true;
    return !dish.allergens?.some((allergen) => selectedAllergens.includes(allergen));
  });

  return (
    <div className="menu-catalog">
      <div className="menu-catalog__filters">
        <label className="menu-catalog__switch">
          <input
            type="checkbox"
            checked={useFitMyDay}
            onChange={(e) => setUseFitMyDay(e.target.checked)}
          />
          <span>Fit my day</span>
        </label>

        <input
          type="number"
          className="menu-catalog__input"
          value={remainingKcal}
          onChange={(e) => setRemainingKcal(e.target.value)}
          disabled={!useFitMyDay}
          placeholder="kcal limit"
        />

        <select
          className="menu-catalog__select"
          value={sortOption}
          onChange={(e) => setSortOption(e.target.value)}
        >
          <option value="none">Select All</option>
          <option value="proteins">High Protein</option>
          <option value="calories">Lowest Calories</option>
          <option value="fats">Lowest Fats</option>
          <option value="carbs">Lowest Carbs</option>
        </select>
      </div>

      {/* Секція тегів алергенів */}
      {dishes.length > 0 && (
        <div className="allergen-tags">
          <span className="allergen-tags__label">Exclude allergens:</span>
          {availableAllergens.map((allergen) => (
            <button
              key={allergen}
              className={`allergen-tag ${selectedAllergens.includes(allergen) ? 'active' : ''}`}
              onClick={() => toggleAllergen(allergen)}
            >
              {allergen} {selectedAllergens.includes(allergen) ? '✕' : ''}
            </button>
          ))}
        </div>
      )}

      <div
        className="menu-content"
        style={{ minHeight: '60vh', display: 'flex', flexDirection: 'column' }}
      >
        {loading ? (
          <div className="spinner-container" style={{ margin: 'auto' }}>
            Loading...
          </div>
        ) : visibleDishes.length > 0 ? (
          <div className="menu-catalog__grid">
            {visibleDishes.map((dish) => (
              <DishCard key={dish.id} dish={dish} onAddToCart={onAddToCart} />
            ))}
          </div>
        ) : (
          /* ЦЕНТРУВАННЯ НАПИСУ */
          <div className="menu-catalog__empty" style={{ margin: 'auto', textAlign: 'center' }}>
            <UtensilsCrossed size={56} className="empty-icon" />
            <h2>Oops! No dishes found</h2>
            <p>Try changing the calorie limit or removing allergen filters.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MenuCatalog;

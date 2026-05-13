import api from '../api/axios';
import React, { useState, useEffect } from 'react';
import DishCard from './DishCard';
import { getAllDishes } from '../services/dishService';
import './MenuCatalog.css';
import { CirclePlus } from 'lucide-react';

const MenuCatalog = ({ dishes: mockDishes, showFavoritesOnly = false }) => {
  const [dishes, setDishes] = useState([]);
  const [useFitMyDay, setUseFitMyDay] = useState(false);
  const [remainingKcal, setRemainingKcal] = useState('500');
  const [sortOption, setSortOption] = useState('none');
  const [searchQuery, setSearchQuery] = useState('');
  const [favoriteIds, setFavoriteIds] = useState([]);

  const [selectedAllergens, setSelectedAllergens] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [availableAllergens, setAvailableAllergens] = useState([]);

  // Load favorites from localStorage on mount
  useEffect(() => {
    const savedFavorites = localStorage.getItem('favoriteIds');
    if (savedFavorites) {
      try {
        setFavoriteIds(JSON.parse(savedFavorites));
      } catch (err) {
        console.error('Error parsing favorites from localStorage:', err);
        setFavoriteIds([]);
      }
    }
  }, []);

  // Save favorites to localStorage whenever they change
  useEffect(() => {
    localStorage.setItem('favoriteIds', JSON.stringify(favoriteIds));
  }, [favoriteIds]);

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

  // Toggle favorite function - adds/removes dish ID from favoriteIds
  const toggleFavorite = (dishId) => {
    setFavoriteIds((prev) =>
      prev.includes(dishId) ? prev.filter((id) => id !== dishId) : [...prev, dishId]
    );
  };

  // Filter by allergens
  const allergenFilteredDishes = dishes.filter((dish) => {
    if (selectedAllergens.length === 0) return true;
    return !dish.allergens?.some((allergen) => selectedAllergens.includes(allergen));
  });

  // Filter by search query (case-insensitive)
  const searchFilteredDishes = allergenFilteredDishes.filter((dish) =>
    dish.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Filter by favorites if showFavoritesOnly is true
  const visibleDishes = showFavoritesOnly
    ? searchFilteredDishes.filter((dish) => favoriteIds.includes(dish.id))
    : searchFilteredDishes;

  return (
    <div className="menu-catalog">
      {/* Only show filters if we have dishes or not in favorites-only mode */}
      {!(showFavoritesOnly && visibleDishes.length === 0) && (
        <>
          <div className="menu-catalog__filters">
            {/* Search Bar */}
            <input
              type="text"
              className="menu-catalog__search"
              placeholder="Search dishes by name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />

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
        </>
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
              <DishCard
                key={dish.id}
                dish={dish}
                isFavorite={favoriteIds.includes(dish.id)}
                onToggleFavorite={toggleFavorite}
              />
            ))}
          </div>
        ) : showFavoritesOnly ? (
          /* Empty state for favorites */
          <div className="menu-catalog__empty" style={{ margin: 'auto', textAlign: 'center' }}>
            <CirclePlus size={56} style={{ color: '#999', marginBottom: '20px' }} />
            <h2>No favorite dishes yet</h2>
            <p>Start adding your favorite dishes by clicking the heart icon on the menu!</p>
          </div>
        ) : (
          /* Empty state for search/filters */
          <div className="menu-catalog__empty" style={{ margin: 'auto', textAlign: 'center' }}>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="56"
              height="56"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              style={{ color: '#999', marginBottom: '20px' }}
            >
              <path d="M3 2v6h6M21 2v6h-6" />
              <path d="M3 13h18M5 22h14" />
            </svg>
            <h2>Oops! No dishes found</h2>
            <p>Try changing the search query, calorie limit, or removing allergen filters.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MenuCatalog;

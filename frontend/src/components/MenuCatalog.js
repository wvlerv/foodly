import React, { useState, useEffect } from 'react';
import DishCard from './DishCard';
import { getAllDishes } from '../services/dishService';
import './MenuCatalog.css';
import { CirclePlus } from 'lucide-react';

// Об'єднуємо всі пропси в один аргумент
const MenuCatalog = ({ dishes: mockDishes, onAddToCart, showFavoritesOnly = false }) => {
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

  // Helper function to get JWT token from localStorage
  const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    };
  };

  // Fetch user's favorite dishes from API on component mount
  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        const response = await fetch('/api/profile/favorites', {
          method: 'GET',
          headers: getAuthHeaders(),
        });

        if (response.ok) {
          const favoriteDishes = await response.json();
          // Extract IDs from the returned dish objects
          const ids = favoriteDishes.map((dish) => dish.id);
          setFavoriteIds(ids);
        } else if (response.status === 401) {
          // User not authenticated, initialize with empty array
          setFavoriteIds([]);
        }
      } catch (err) {
        console.error('Error fetching favorites:', err);
        setFavoriteIds([]);
      }
    };

    fetchFavorites();
  }, []);

  const mapSortOptionToParams = (option) => {
    const mapping = {
      proteins: 'proteins',
      calories: 'calories',
      fats: 'fats',
      carbs: 'carbs',
    };
    return { sortBy: mapping[option] || null };
  };

  const collectAllergens = (dishList) => {
    const values = new Set();
    dishList.forEach((dish) => {
      dish.allergens?.forEach((allergen) => values.add(allergen));
    });
    return Array.from(values).sort();
  };

  // Отримання даних з API
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
        console.error('Помилка завантаження:', err);
        setDishes(mockDishes || []);
        setError('Could not load fresh menu. Showing offline version.');
      } finally {
        setLoading(false);
      }
    };
    fetchDishes();
  }, [useFitMyDay, remainingKcal, sortOption, mockDishes]);

  const toggleAllergen = (allergen) => {
    setSelectedAllergens((prev) =>
      prev.includes(allergen) ? prev.filter((a) => a !== allergen) : [...prev, allergen]
    );
  };

  /**
   * Toggle favorite status for a dish using the API.
   * If the dish is already a favorite, delete it.
   * If not, add it to favorites.
   */
  const toggleFavorite = async (dishId) => {
    const isFavorited = favoriteIds.includes(dishId);
    const method = isFavorited ? 'DELETE' : 'POST';
    const url = `/api/profile/favorites/${dishId}`;

    try {
      console.log(
        `[Favorites] ${method} request to ${url}, Token: ${localStorage.getItem('token') ? 'present' : 'missing'}`
      );

      const response = await fetch(url, {
        method,
        headers: getAuthHeaders(),
      });

      console.log(`[Favorites] Response status: ${response.status}`);

      if (response.ok) {
        // Update state only if fetch was successful
        if (isFavorited) {
          // Remove from favorites
          setFavoriteIds((prev) => prev.filter((id) => id !== dishId));
          console.log('[Favorites] Removed from favorites');
        } else {
          // Add to favorites
          setFavoriteIds((prev) => [...prev, dishId]);
          console.log('[Favorites] Added to favorites');
        }
      } else if (response.status === 401) {
        console.error('[Favorites] Not authenticated (401)');
        alert('Please log in to add items to favorites');
      } else {
        const errorText = await response.text();
        console.error(`[Favorites] Error (${response.status}): ${response.statusText}`, errorText);
        alert(`Error: ${response.status} - ${response.statusText}`);
      }
    } catch (err) {
      console.error('[Favorites] Network error:', err);
      alert(`Network error: ${err.message}`);
    }
  };

  // Фільтрація (Алергени -> Пошук -> Обране)
  const visibleDishes = dishes
    .filter((dish) => {
      if (selectedAllergens.length === 0) return true;
      return !dish.allergens?.some((a) => selectedAllergens.includes(a));
    })
    .filter((dish) => dish.name.toLowerCase().includes(searchQuery.toLowerCase()))
    .filter((dish) => (showFavoritesOnly ? favoriteIds.includes(dish.id) : true));

  return (
    <div className="menu-catalog">
      {/* Фільтри показуємо лише якщо не в режимі порожнього "Обраного" */}
      {!(showFavoritesOnly && visibleDishes.length === 0) && (
        <>
          <div className="menu-catalog__filters">
            <input
              type="text"
              className="menu-catalog__search"
              placeholder="Search dishes by name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />

            <div className="menu-catalog__controls">
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
                <option value="none">Sort By</option>
                <option value="proteins">High Protein</option>
                <option value="calories">Lowest Calories</option>
                <option value="fats">Lowest Fats</option>
                <option value="carbs">Lowest Carbs</option>
              </select>
            </div>
          </div>

          {availableAllergens.length > 0 && (
            <div className="allergen-tags">
              <span className="allergen-tags__label">Exclude:</span>
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

      <div className="menu-content" style={{ minHeight: '60vh' }}>
        {loading ? (
          <div className="spinner-container">Loading menu...</div>
        ) : visibleDishes.length > 0 ? (
          <div className="menu-catalog__grid">
            {visibleDishes.map((dish) => (
              <DishCard
                key={dish.id}
                dish={dish}
                onAddToCart={onAddToCart} // Передаємо функцію кошика
                isFavorite={favoriteIds.includes(dish.id)} // Передаємо статус серця
                onToggleFavorite={toggleFavorite} // Передаємо функцію кліку по серцю
              />
            ))}
          </div>
        ) : (
          <div className="menu-catalog__empty">
            {showFavoritesOnly ? (
              <>
                <CirclePlus size={56} color="#999" />
                <h2>No favorite dishes yet</h2>
                <p>Click the heart icon on any dish to save it here!</p>
              </>
            ) : (
              <>
                <CirclePlus size={56} color="#999" />
                <h2>Oops! No dishes found</h2>
                <p>Try adjusting your filters or search query.</p>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default MenuCatalog;

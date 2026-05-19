import React, { useMemo, useState, useEffect } from 'react';
import DishCard from './DishCard';
import { getAllDishes, toggleDishAvailability } from '../services/dishService';
import api from '../api/axios';
import './MenuCatalog.css';
import { CirclePlus, Info } from 'lucide-react';
import { calculateRemainingCalories, getCaloriesTotal } from '../utils/nutritionUtils';

const warningOverlayStyle = {
  position: 'fixed',
  inset: 0,
  zIndex: 1000,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: 'rgba(15, 23, 42, 0.65)',
  padding: '16px',
};

const warningModalStyle = {
  width: 'min(100%, 440px)',
  borderRadius: '20px',
  backgroundColor: '#ffffff',
  padding: '24px',
  boxShadow: '0 24px 80px rgba(0, 0, 0, 0.28)',
  textAlign: 'center',
};

const warningModalActionsStyle = {
  display: 'flex',
  gap: '12px',
  justifyContent: 'center',
  marginTop: '24px',
};

const warningModalCancelStyle = {
  border: '1px solid #d1d5db',
  backgroundColor: '#ffffff',
  color: '#111827',
  borderRadius: '999px',
  padding: '10px 16px',
  fontWeight: 600,
  cursor: 'pointer',
};

const warningModalConfirmStyle = {
  border: 'none',
  backgroundColor: '#dc2626',
  color: '#ffffff',
  borderRadius: '999px',
  padding: '10px 16px',
  fontWeight: 700,
  cursor: 'pointer',
};

const normalizeAllergen = (value) =>
  String(value ?? '')
    .trim()
    .toLowerCase();

const sortDishesByCalories = (dishList = []) =>
  [...dishList].sort((left, right) => Number(left?.calories || 0) - Number(right?.calories || 0));

// Об'єднуємо всі пропси в один аргумент
const MenuCatalog = ({
  dishes: mockDishes,
  onAddToCart,
  showFavoritesOnly = false,
  onShowErrorToast,
  cartItems = [],
  nutritionSummary = { dailyCalorieIntake: 2000, deliveredCalories: 0 },
}) => {
  const [dishes, setDishes] = useState([]);
  const [useFitMyDay, setUseFitMyDay] = useState(false);
  const [remainingKcal, setRemainingKcal] = useState('');
  const [manualLimitEdited, setManualLimitEdited] = useState(false);
  const [sortOption, setSortOption] = useState('none');
  const [searchQuery, setSearchQuery] = useState('');
  const [favoriteIds, setFavoriteIds] = useState([]);
  const [selectedAllergens, setSelectedAllergens] = useState([]);
  const [userAllergens, setUserAllergens] = useState([]);
  const [warningDish, setWarningDish] = useState(null);
  const [loading, setLoading] = useState(true);
  const [, setError] = useState(null);
  const [availableAllergens, setAvailableAllergens] = useState([]);
  const [fitDayNotice, setFitDayNotice] = useState('');

  const cartCalories = useMemo(() => getCaloriesTotal(cartItems), [cartItems]);

  const recommendedRemainingKcal = useMemo(
    () =>
      calculateRemainingCalories({
        dailyCalorieIntake: nutritionSummary?.dailyCalorieIntake,
        deliveredCalories: nutritionSummary?.deliveredCalories,
        cartCalories,
      }),
    [cartCalories, nutritionSummary?.dailyCalorieIntake, nutritionSummary?.deliveredCalories]
  );

  const numericRemainingKcal = Number(remainingKcal);
  const hasNumericRemainingKcal = remainingKcal !== '' && Number.isFinite(numericRemainingKcal);
  const clampedRemainingKcal = useFitMyDay
    ? Math.max(0, hasNumericRemainingKcal ? numericRemainingKcal : recommendedRemainingKcal)
    : remainingKcal;
  const isLowLimitMode = useFitMyDay && recommendedRemainingKcal <= 0;
  const isFitDayInputLocked = useFitMyDay && recommendedRemainingKcal <= 0;

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
        const response = await api.get('/profile/favorites', {
          headers: getAuthHeaders(),
        });

        const favoriteDishes = response.data;
        // Extract IDs from the returned dish objects
        const ids = favoriteDishes.map((dish) => dish.id);
        setFavoriteIds(ids);
      } catch (err) {
        if (err.response?.status === 401) {
          // User not authenticated, initialize with empty array
          setFavoriteIds([]);
          return;
        }

        console.error('Error fetching favorites:', err);
        setFavoriteIds([]);
      }
    };

    fetchFavorites();
  }, []);

  useEffect(() => {
    const fetchUserAllergens = async () => {
      try {
        const response = await api.get('/profile/me', {
          headers: getAuthHeaders(),
        });

        const allergens = Array.isArray(response.data?.allergens) ? response.data.allergens : [];
        setUserAllergens(
          allergens
            .filter((allergen) => allergen !== null && allergen !== undefined)
            .map((allergen) => String(allergen).trim())
            .filter((allergen) => allergen !== '' && allergen !== 'string')
        );
      } catch (err) {
        setUserAllergens([]);
      }
    };

    fetchUserAllergens();
  }, []);

  useEffect(() => {
    if (!useFitMyDay) {
      return;
    }

    if (recommendedRemainingKcal <= 0) {
      setRemainingKcal('0');
      setManualLimitEdited(false);
      return;
    }

    if (!manualLimitEdited || numericRemainingKcal <= 0) {
      setRemainingKcal(String(recommendedRemainingKcal));
      setManualLimitEdited(false);
    }
  }, [manualLimitEdited, numericRemainingKcal, recommendedRemainingKcal, useFitMyDay]);

  const mapSortOptionToParams = (option) => {
    const mapping = {
      proteins: 'proteins',
      calories: 'calories',
      fats: 'fats',
      carbs: 'carbs',
    };
    return { sortBy: mapping[option] || null };
  };

  const getMatchedAllergens = (dishAllergens = []) => {
    const userAllergenSet = new Set(userAllergens.map(normalizeAllergen).filter(Boolean));

    return (Array.isArray(dishAllergens) ? dishAllergens : [])
      .filter((allergen) => allergen !== null && allergen !== undefined)
      .map((allergen) => String(allergen).trim())
      .filter((allergen) => allergen !== '' && allergen !== 'string')
      .filter((allergen) => userAllergenSet.has(normalizeAllergen(allergen)));
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
        const { sortBy } = mapSortOptionToParams(sortOption);
        const appliedKcal = useFitMyDay && hasNumericRemainingKcal ? numericRemainingKcal : null;

        const data = await getAllDishes(appliedKcal, sortBy);

        setDishes(data);
        setAvailableAllergens(collectAllergens(data));
        setError(null);

        if (isLowLimitMode) {
          setFitDayNotice(
            'You have already reached your daily calorie limit. Showing the lightest dishes in the catalog.'
          );
        } else {
          setFitDayNotice('');
        }
      } catch (err) {
        console.error('Помилка завантаження:', err);
        const fallbackDishes = isLowLimitMode
          ? sortDishesByCalories(mockDishes || [])
          : mockDishes || [];
        setDishes(fallbackDishes);
        setError('Could not load fresh menu. Showing offline version.');
        setFitDayNotice(
          isLowLimitMode
            ? 'You have already reached your daily calorie limit. Showing the lightest dishes in the catalog.'
            : ''
        );
      } finally {
        setLoading(false);
      }
    };
    fetchDishes();
  }, [
    hasNumericRemainingKcal,
    isLowLimitMode,
    mockDishes,
    numericRemainingKcal,
    sortOption,
    useFitMyDay,
  ]);

  const toggleAllergen = (allergen) => {
    setSelectedAllergens((prev) =>
      prev.includes(allergen) ? prev.filter((item) => item !== allergen) : [...prev, allergen]
    );
  };

  const handleAddToCart = (dish) => {
    const token = localStorage.getItem('token');
    const hasValidToken = token && token !== 'undefined' && token !== 'null';
    if (!hasValidToken) {
      if (typeof onShowErrorToast === 'function') {
        onShowErrorToast('Log in to order');
      } else {
        alert('Log in to order');
      }
      return;
    }

    const matched = getMatchedAllergens(dish.allergens);
    if (matched.length > 0) {
      setWarningDish({ dish, matchedAllergens: matched });
    } else {
      onAddToCart?.(dish);
    }
  };

  const handleCancelWarning = () => {
    setWarningDish(null);
  };

  const handleAddAnyway = () => {
    if (warningDish?.dish) {
      onAddToCart?.(warningDish.dish);
    }
    setWarningDish(null);
  };

  const handleStatusChange = async (dishId, newStatus) => {
    try {
      await toggleDishAvailability(dishId, newStatus);
      setDishes((prevDishes) =>
        prevDishes.map((d) =>
          d.id === dishId ? { ...d, isAvailable: newStatus, available: newStatus } : d
        )
      );
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to update dish availability';
      onShowErrorToast(errMsg);
    }
  };

  /**
   * Toggle favorite status for a dish using the API.
   * If the dish is already a favorite, delete it.
   * If not, add it to favorites.
   */
  const toggleFavorite = async (dishId) => {
    const isFavorited = favoriteIds.includes(dishId);
    const method = isFavorited ? 'delete' : 'post';
    const url = `/profile/favorites/${dishId}`;
    const token = localStorage.getItem('token');

    if (!token || token === 'undefined' || token === 'null') {
      onShowErrorToast('Please log in to like dishes.');
      return;
    }

    try {
      console.log(
        `[Favorites] ${method} request to ${url}, Token: ${token ? 'present' : 'missing'}`
      );

      const response = isFavorited
        ? await api.delete(url, { headers: getAuthHeaders() })
        : await api.post(url, null, { headers: getAuthHeaders() });

      console.log(`[Favorites] Response status: ${response.status}`);

      if (response.status >= 200 && response.status < 300) {
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
        onShowErrorToast('Please log in to add items to favorites');
      }
    } catch (err) {
      if (err.response?.status === 401) {
        console.error('[Favorites] Not authenticated (401)');
        onShowErrorToast('Please log in to add items to favorites');
        return;
      }

      console.error('[Favorites] Network error:', err);
      onShowErrorToast(`Network error: ${err.message}`);
    }
  };

  // Фільтрація: exclude allergens -> search -> favorites
  const visibleDishes = dishes
    .filter((dish) => {
      if (selectedAllergens.length === 0) return true;
      const dishAllergens = (Array.isArray(dish.allergens) ? dish.allergens : [])
        .map((allergen) => normalizeAllergen(allergen))
        .filter(Boolean);
      const excludedAllergens = new Set(
        selectedAllergens.map((allergen) => normalizeAllergen(allergen))
      );

      return !dishAllergens.some((allergen) => excludedAllergens.has(allergen));
    })
    .filter((dish) => dish.name.toLowerCase().includes(searchQuery.toLowerCase()))
    .filter((dish) => (showFavoritesOnly ? favoriteIds.includes(dish.id) : true))
    .filter((dish) => {
      if (!useFitMyDay || isLowLimitMode || !hasNumericRemainingKcal) {
        return true;
      }

      return Number(dish.calories || 0) <= numericRemainingKcal;
    });

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
                  onChange={(e) => {
                    setUseFitMyDay(e.target.checked);
                    setManualLimitEdited(false);
                  }}
                />
                <span>Fit my day</span>
              </label>

              <input
                type="number"
                className="menu-catalog__input"
                value={clampedRemainingKcal}
                onChange={(e) => {
                  const parsedValue = Number(e.target.value);
                  setRemainingKcal(
                    String(Number.isFinite(parsedValue) ? Math.max(0, parsedValue) : 0)
                  );
                  setManualLimitEdited(true);
                }}
                disabled={!useFitMyDay || isFitDayInputLocked}
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

          {fitDayNotice && (
            <div className="menu-catalog__fit-day-banner" role="status" aria-live="polite">
              <div className="menu-catalog__fit-day-banner-icon" aria-hidden="true">
                <Info size={18} />
              </div>
              <div className="menu-catalog__fit-day-banner-copy">
                <strong>Fit my day</strong>
                <span>{fitDayNotice}</span>
              </div>
            </div>
          )}

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
              <div key={dish.id}>
                <DishCard
                  dish={dish}
                  onAddToCart={handleAddToCart}
                  isFavorite={favoriteIds.includes(dish.id)}
                  onToggleFavorite={toggleFavorite}
                  matchedAllergens={getMatchedAllergens(dish.allergens)}
                  onToggleAvailability={handleStatusChange}
                />
              </div>
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

      {warningDish && (
        <div
          className="menu-catalog__warning-modal"
          role="presentation"
          style={warningOverlayStyle}
        >
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="allergen-warning-title"
            style={warningModalStyle}
          >
            <h2 id="allergen-warning-title">Allergen warning</h2>
            <p>
              This dish contains {warningDish.matchedAllergens.join(', ')}. Are you sure you want to
              add it?
            </p>

            <div style={warningModalActionsStyle}>
              <button type="button" onClick={handleCancelWarning} style={warningModalCancelStyle}>
                Cancel
              </button>
              <button type="button" onClick={handleAddAnyway} style={warningModalConfirmStyle}>
                Add Anyway
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MenuCatalog;

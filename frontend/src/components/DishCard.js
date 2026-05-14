import React from 'react';
import { AlertCircle, AlertTriangle, Heart, ShoppingCart } from 'lucide-react'; // Додав ShoppingCart для однотипності
import './DishCard.css';

const DishCard = ({
  dish,
  onAddToCart,
  selectedAllergen = '',
  isFavorite = false,
  onToggleFavorite = null,
}) => {
  // 1. Безпечне отримання значень (якщо раптом якогось поля немає, ставимо 0)
  const {
    name = 'Unknown Dish',
    price = 0,
    proteins = 0,
    fats = 0,
    carbohydrates = 0,
    calories = 0,
    imageUrl = '',
  } = dish;

  const isAvailable = dish.available ?? dish.isAvailable ?? true;
  const allergens = Array.isArray(dish.allergens) ? dish.allergens : [];

  const normalizedSelectedAllergen = selectedAllergen.trim().toLowerCase();
  const matchedAllergen = normalizedSelectedAllergen
    ? allergens.find((a) => a.toLowerCase() === normalizedSelectedAllergen)
    : null;

  // 2. Розрахунок калорійності макросів (4-9-4 правило)
  const totalMacroCalories = proteins * 4 + carbohydrates * 4 + fats * 9;

  const getPercentage = (val, multiplier) =>
    totalMacroCalories > 0 ? Math.round(((val * multiplier) / totalMacroCalories) * 100) : 0;

  const proteinPercentage = getPercentage(proteins, 4);
  const carbsPercentage = getPercentage(carbohydrates, 4);
  const fatsPercentage = getPercentage(fats, 9);

  return (
    <div className={`dish-card ${!isAvailable ? 'dish-card--unavailable' : ''}`}>
      <div className="dish-card__image-wrapper">
        <img src={imageUrl} alt={name} className="dish-card__image" />

        {!isAvailable && <div className="dish-card__unavailable">Out of Stock</div>}

        {matchedAllergen && (
          <div
            className="dish-card__allergen-alert"
            title={`Contains allergen: ${matchedAllergen}`}
          >
            <AlertCircle size={32} color="#ff4444" fill="#ff4444" />
          </div>
        )}

        <button
          className={`dish-card__favorite-btn ${isFavorite ? 'active' : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite?.(dish.id);
          }}
        >
          <Heart
            size={28}
            fill={isFavorite ? '#ff4444' : 'none'}
            color={isFavorite ? '#ff4444' : '#666'}
          />
        </button>
      </div>

      <div className="dish-card__body">
        <h3 className="dish-card__name">{name}</h3>
        <p className="dish-card__description">{dish.description}</p>

        <div className="dish-card__meta-row">
          {/* Додав Number() та перевірку, щоб toFixed не видавав помилку */}
          <div className="dish-card__price">${Number(price).toFixed(2)}</div>

          {allergens.length > 0 && (
            <div className="dish-card__allergen-badges">
              {allergens.slice(0, 2).map(
                (
                  allergen // Обмежив до 2, щоб не ламало верстку
                ) => (
                  <span className="dish-card__allergen-badge" key={allergen}>
                    <AlertTriangle size={14} /> {allergen}
                  </span>
                )
              )}
            </div>
          )}
        </div>

        <div className="dish-card__nutrition">
          <div className="nutrition-summary">
            <strong>{Math.round(calories)}</strong> <small>kcal</small>
          </div>

          <div className="nutrition-bars">
            {/* Рендеримо бари (код такий самий, але чистіший) */}
            {[
              { label: 'Prot.', val: proteins, pct: proteinPercentage, class: 'protein' },
              { label: 'Fats', val: fats, pct: fatsPercentage, class: 'fat' },
              { label: 'Carbs', val: carbohydrates, pct: carbsPercentage, class: 'carbs' },
            ].map((item) => (
              <div className="nutrition-item" key={item.label}>
                <div className="nutrition-item__info">
                  <span>{item.label}</span>
                  <span>{Math.round(item.val)}g</span>
                </div>
                <div className="nutrition-item__bar">
                  <div
                    className={`nutrition-item__bar-fill ${item.class}-bar`}
                    style={{ width: `${item.pct}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <button
          className="dish-card__add-btn"
          onClick={() => isAvailable && onAddToCart(dish)}
          disabled={!isAvailable}
        >
          <ShoppingCart size={18} />
          <span>{isAvailable ? 'Add to Order' : 'Unavailable'}</span>
        </button>
      </div>
    </div>
  );
};

export default DishCard;

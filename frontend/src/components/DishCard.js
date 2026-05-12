import React from 'react';
import { AlertCircle, AlertTriangle } from 'lucide-react';
import './DishCard.css';

const DishCard = ({ dish, onAddToCart, selectedAllergen = '' }) => {
  const isAvailable = dish.available ?? dish.isAvailable ?? true;
  const allergens = Array.isArray(dish.allergens) ? dish.allergens : [];
  const normalizedSelectedAllergen = selectedAllergen.trim().toLowerCase();
  const matchedAllergen = allergens.find(
    (allergen) => allergen.toLowerCase() === normalizedSelectedAllergen
  );

  const handleAddToCart = () => {
    if (isAvailable) {
      onAddToCart(dish); // Викликаємо функцію з App.js
    } else {
      alert(`Sorry, ${dish.name} is currently out of stock.`);
    }
  };

  // Calculate macronutrient percentages
  const totalMacroCalories = dish.proteins * 4 + dish.carbohydrates * 4 + dish.fats * 9;
  const proteinPercentage =
    totalMacroCalories > 0 ? Math.round(((dish.proteins * 4) / totalMacroCalories) * 100) : 0;
  const carbsPercentage =
    totalMacroCalories > 0 ? Math.round(((dish.carbohydrates * 4) / totalMacroCalories) * 100) : 0;
  const fatsPercentage =
    totalMacroCalories > 0 ? Math.round(((dish.fats * 9) / totalMacroCalories) * 100) : 0;

  return (
    <div className={`dish-card ${!isAvailable ? 'dish-card--unavailable' : ''}`}>
      <div className="dish-card__image-wrapper">
        <img src={dish.imageUrl} alt={dish.name} className="dish-card__image" />
        {!isAvailable && <div className="dish-card__unavailable">Out of Stock</div>}
        {matchedAllergen && (
          <div className="dish-card__allergen-alert" title={`Contains allergen: ${matchedAllergen}`}>
            <AlertCircle size={32} color="#ff4444" fill="#ff4444" />
          </div>
        )}
      </div>

      <div className="dish-card__body">
        <h3 className="dish-card__name">{dish.name}</h3>
        <p className="dish-card__description">{dish.description}</p>

        <div className="dish-card__meta-row">
          <div className="dish-card__price">${dish.price.toFixed(2)}</div>
          {allergens.length > 0 && (
            <div className="dish-card__allergen-badges">
              {allergens.map((allergen) => (
                <span className="dish-card__allergen-badge" key={allergen}>
                  <AlertTriangle size={16} style={{ marginRight: '4px' }} /> {allergen}
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="dish-card__nutrition">
          <div className="nutrition-row">
            <span className="nutrition-label">Calories:</span>
            <span className="nutrition-value">{Math.round(dish.calories)}</span>
          </div>
          <div className="nutrition-grid">
            <div className="nutrition-item">
              <div className="nutrition-item__label">Proteins</div>
              <div className="nutrition-item__value">{Math.round(dish.proteins)}g</div>
              <div className="nutrition-item__percentage">{proteinPercentage}%</div>
              <div className="nutrition-item__bar">
                <div
                  className="nutrition-item__bar-fill protein-bar"
                  style={{ width: `${proteinPercentage}%` }}
                ></div>
              </div>
            </div>
            <div className="nutrition-item">
              <div className="nutrition-item__label">Fats</div>
              <div className="nutrition-item__value">{Math.round(dish.fats)}g</div>
              <div className="nutrition-item__percentage">{fatsPercentage}%</div>
              <div className="nutrition-item__bar">
                <div
                  className="nutrition-item__bar-fill fat-bar"
                  style={{ width: `${fatsPercentage}%` }}
                ></div>
              </div>
            </div>
            <div className="nutrition-item">
              <div className="nutrition-item__label">Carbs</div>
              <div className="nutrition-item__value">{Math.round(dish.carbohydrates)}g</div>
              <div className="nutrition-item__percentage">{carbsPercentage}%</div>
              <div className="nutrition-item__bar">
                <div
                  className="nutrition-item__bar-fill carbs-bar"
                  style={{ width: `${carbsPercentage}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>

        {/* НОВА КНОПКА */}
        <button className="dish-card__add-btn" onClick={handleAddToCart} disabled={!isAvailable}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
          <span>Add to Order</span>
        </button>
      </div>
    </div>
  );
};

export default DishCard;

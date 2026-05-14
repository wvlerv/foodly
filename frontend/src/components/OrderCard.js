import React from 'react';
import './DishCard.css';
import './OrderCard.css';

const statusColors = {
  CREATED: '#6b7280', // gray - newly created
  PAID: '#3b82f6', // blue - payment confirmed
  PREPARING: '#f59e0b', // amber - being prepared
  DELIVERED: '#10b981', // green - delivered
  CANCELLED: '#ef4444', // red - cancelled
};

const OrderCard = ({ order }) => {
  const created = order.createdAt ? new Date(order.createdAt).toLocaleString() : '—';
  const status = order.status || 'UNKNOWN';

  // display status as-is without grouping all 5 statuses distinctly
  const displayStatus = (status && status.toUpperCase()) || 'UNKNOWN';

  const calories = order.calories ? Math.round(order.calories) : 0;
  const proteins = order.proteins ? Math.round(order.proteins) : 0;
  const fats = order.fats ? Math.round(order.fats) : 0;
  const carbs = order.carbohydrates ? Math.round(order.carbohydrates) : 0;

  // Розрахунок калорійності макросів (4-9-4 правило)
  const totalMacroCalories = proteins * 4 + carbs * 4 + fats * 9;

  const getPercentage = (val, multiplier) =>
    totalMacroCalories > 0 ? Math.round(((val * multiplier) / totalMacroCalories) * 100) : 0;

  const proteinPercentage = getPercentage(proteins, 4);
  const carbsPercentage = getPercentage(carbs, 4);
  const fatsPercentage = getPercentage(fats, 9);

  return (
    <div className="dish-card order-card">
      <div className="dish-card__body">
        <h3 className="dish-card__name order-card__id">Order #{String(order.id).slice(0, 8)}</h3>
        <p className="dish-card__description">Placed: {created}</p>

        <div className="dish-card__meta-row">
          <div className="order-card__status-row">
            <div
              className="order-card__status-dot"
              style={{ background: statusColors[status] || '#6b7280' }}
            />
            <div
              className="order-card__status-text"
              style={{ color: statusColors[status] || '#6b7280' }}
            >
              {displayStatus}
            </div>
          </div>

          <div className="order-card__price">
            ${order.totalPrice ? Number(order.totalPrice).toFixed(2) : '0.00'}
          </div>
        </div>

        <div className="dish-card__nutrition">
          <div className="nutrition-summary">
            <strong>{calories}</strong> <small>kcal</small>
          </div>

          <div className="nutrition-bars">
            {[
              { label: 'Prot.', val: proteins, pct: proteinPercentage, class: 'protein' },
              { label: 'Fats', val: fats, pct: fatsPercentage, class: 'fat' },
              { label: 'Carbs', val: carbs, pct: carbsPercentage, class: 'carbs' },
            ].map((item) => (
              <div className="nutrition-item" key={item.label}>
                <div className="nutrition-item__info">
                  <span>{item.label}</span>
                  <span>{item.val}g</span>
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

        {order.items && order.items.length > 0 && (
          <div className="order-card__items">
            <div className="order-card__items-title">Items</div>
            <ul className="order-card__items-list">
              {order.items.map((it, idx) => {
                // support older string-format items gracefully
                const item =
                  typeof it === 'string' ? { name: it, quantity: 1, imageUrl: null } : it;
                return (
                  <li className="order-card__item" key={idx}>
                    <div className="order-card__item-thumb">
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.name} />
                      ) : (
                        <div className="order-card__item-fallback" />
                      )}
                    </div>
                    <div className="order-card__item-meta">
                      <div className="order-card__item-name">{item.name}</div>
                      <div className="order-card__item-qty">x{item.quantity}</div>
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderCard;

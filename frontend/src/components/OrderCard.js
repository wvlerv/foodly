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
          <div className="nutrition-row">
            <span className="nutrition-label">Total Calories</span>
            <span className="nutrition-value">{calories} kcal</span>
          </div>

          <div className="nutrition-grid">
            <div className="nutrition-item">
              <div className="nutrition-item__label">Proteins</div>
              <div className="nutrition-item__value">{proteins}g</div>
            </div>
            <div className="nutrition-item">
              <div className="nutrition-item__label">Fats</div>
              <div className="nutrition-item__value">{fats}g</div>
            </div>
            <div className="nutrition-item">
              <div className="nutrition-item__label">Carbs</div>
              <div className="nutrition-item__value">{carbs}g</div>
            </div>
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

import React from 'react';
import { Trash2, Plus, Minus, ShoppingBag } from 'lucide-react';
import './Cart.css';
import { useNavigate } from 'react-router-dom';

const Cart = ({ items, onUpdateQuantity, onRemove, onCheckout, onShowErrorToast }) => {
  const navigate = useNavigate(); // Використовуємо цей хук для переходів
  const total = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  // Функція, яка спрацьовує при оформленні замовлення
  const handleCheckout = async () => {
    try {
      await onCheckout();
      navigate('/orders');
    } catch (error) {
      onShowErrorToast(error.message || 'Could not place order. Please try again.');
    }
  };

  if (items.length === 0) {
    return (
      <div className="cart-empty">
        <ShoppingBag size={56} className="cart-empty__icon" />
        <h2>Your cart is empty</h2>
        <p>Looks like you haven't chosen anything yet. Let's fix that!</p>
        <button className="cart-empty__btn" onClick={() => navigate('/menu')}>
          Go to menu
        </button>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <h1>Your Order</h1>
      <div className="cart-container">
        <div className="cart-items">
          {items.map((item) => (
            <div key={item.id} className="cart-item">
              <img src={item.imageUrl} alt={item.name} className="cart-item__image" />
              <div className="cart-item__info">
                <h3>{item.name}</h3>
                <p className="cart-item__price">${item.price.toFixed(2)}</p>
              </div>
              <div className="cart-item__controls">
                <button onClick={() => onUpdateQuantity(item.id, -1)} disabled={item.quantity <= 1}>
                  <Minus size={18} />
                </button>
                <span>{item.quantity}</span>
                <button onClick={() => onUpdateQuantity(item.id, 1)}>
                  <Plus size={18} />
                </button>
              </div>
              <div className="cart-item__subtotal">${(item.price * item.quantity).toFixed(2)}</div>
              <button className="cart-item__remove" onClick={() => onRemove(item.id)}>
                <Trash2 size={20} />
              </button>
            </div>
          ))}
        </div>

        <div className="cart-summary">
          <h2>Summary</h2>
          <div className="summary-row">
            <span>Items ({items.length})</span>
            <span>${total.toFixed(2)}</span>
          </div>
          <div className="summary-row summary-total">
            <span>Total amount</span>
            <span>${total.toFixed(2)}</span>
          </div>
          {/* Викликаємо нашу нову функцію handleCheckout */}
          <button className="checkout-btn" onClick={handleCheckout}>
            Checkout
          </button>
        </div>
      </div>
    </div>
  );
};

export default Cart;

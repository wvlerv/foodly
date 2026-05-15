import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, CreditCard, MapPin, Phone, Package } from 'lucide-react';
import api from '../api/axios';
import './CheckoutPage.css';

const CheckoutPage = ({ items, onClearCart, onShowSuccessToast, onShowErrorToast }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    phoneNumber: '',
    city: '',
    street: '',
    house: '',
    apartment: '',
    paymentMethod: 'cash',
  });

  const summary = useMemo(() => {
    const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
    const totalAmount = items.reduce(
      (sum, item) => sum + Number(item.price || 0) * item.quantity,
      0
    );
    const totalCalories = items.reduce(
      (sum, item) => sum + Number(item.calories || 0) * item.quantity,
      0
    );

    return {
      totalItems,
      totalAmount,
      totalCalories,
    };
  }, [items]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handlePlaceOrder = async (event) => {
    event.preventDefault();

    const token = localStorage.getItem('token');

    if (!token || token === 'undefined' || token === 'null') {
      onShowErrorToast('Please log in to place an order.');
      navigate('/login', { replace: true });
      return;
    }

    if (!items.length) {
      onShowErrorToast('Your cart is empty.');
      navigate('/cart', { replace: true });
      return;
    }

    if (!formData.phoneNumber.trim()) {
      onShowErrorToast('Phone number is required.');
      return;
    }

    if (!formData.city.trim() || !formData.street.trim() || !formData.house.trim()) {
      onShowErrorToast('Please fill in city, street and house.');
      return;
    }

    const deliveryAddress = [
      formData.city.trim(),
      formData.street.trim(),
      `house ${formData.house.trim()}`,
      formData.apartment.trim() ? `apt. ${formData.apartment.trim()}` : null,
    ]
      .filter(Boolean)
      .join(', ');

    const payload = {
      deliveryAddress,
      contactPhone: formData.phoneNumber,
      paymentMethod: formData.paymentMethod,
      items: items.map((item) => ({
        dishId: item.id,
        quantity: item.quantity,
      })),
    };

    try {
      await api.post('/orders', payload, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      onClearCart();
      onShowSuccessToast('Order placed successfully!');
      navigate('/orders', { replace: true });
    } catch (error) {
      const backendMessage =
        error?.response?.data?.message || error?.response?.data || 'Could not place order.';
      onShowErrorToast(
        typeof backendMessage === 'string' ? backendMessage : 'Could not place order.'
      );
    }
  };

  if (!items.length) {
    return (
      <div className="checkout-page checkout-page--empty">
        <div className="checkout-empty-card">
          <Package size={52} />
          <h1>Your cart is empty</h1>
          <p>Add some dishes before going to checkout.</p>
          <button type="button" className="checkout-back-btn" onClick={() => navigate('/menu')}>
            Go to menu
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <div className="checkout-page__header">
        <button type="button" className="checkout-back-link" onClick={() => navigate('/cart')}>
          <ArrowLeft size={18} />
          Back to cart
        </button>
        <h1>Checkout</h1>
        <p>Finish your order with delivery details and payment method.</p>
      </div>

      <div className="checkout-layout">
        <section className="checkout-card checkout-form-card">
          <h2>Delivery details</h2>
          <form className="checkout-form" onSubmit={handlePlaceOrder}>
            <label className="checkout-field">
              <span>
                <Phone size={16} /> Phone number
              </span>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="+1 555 123 4567"
                required
              />
            </label>

            <div className="checkout-grid checkout-grid--two">
              <label className="checkout-field">
                <span>
                  <MapPin size={16} /> City
                </span>
                <input
                  type="text"
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  placeholder="New York"
                  required
                />
              </label>

              <label className="checkout-field">
                <span>
                  <MapPin size={16} /> Street
                </span>
                <input
                  type="text"
                  name="street"
                  value={formData.street}
                  onChange={handleChange}
                  placeholder="5th Avenue"
                  required
                />
              </label>
            </div>

            <div className="checkout-grid checkout-grid--three">
              <label className="checkout-field">
                <span>House</span>
                <input
                  type="text"
                  name="house"
                  value={formData.house}
                  onChange={handleChange}
                  placeholder="24B"
                  required
                />
              </label>

              <label className="checkout-field">
                <span>Apt.</span>
                <input
                  type="text"
                  name="apartment"
                  value={formData.apartment}
                  onChange={handleChange}
                  placeholder="12"
                />
              </label>

              <label className="checkout-field">
                <span>
                  <CreditCard size={16} /> Payment method
                </span>
                <select name="paymentMethod" value={formData.paymentMethod} onChange={handleChange}>
                  <option value="cash">Cash</option>
                  <option value="card-online">Card online</option>
                </select>
              </label>
            </div>

            <button type="submit" className="checkout-submit-btn">
              Place Order
            </button>
          </form>
        </section>

        <aside className="checkout-card checkout-summary-card">
          <h2>Order Summary</h2>

          <div className="checkout-summary-stats">
            <div className="checkout-summary-row">
              <span>Items</span>
              <strong>{summary.totalItems}</strong>
            </div>
            <div className="checkout-summary-row">
              <span>Total</span>
              <strong>${summary.totalAmount.toFixed(2)}</strong>
            </div>
            <div className="checkout-summary-row">
              <span>Calories</span>
              <strong>{Math.round(summary.totalCalories)} kcal</strong>
            </div>
          </div>

          <div className="checkout-summary-items">
            {items.map((item) => (
              <div key={item.id} className="checkout-summary-item">
                <div>
                  <p>{item.name}</p>
                  <span>x{item.quantity}</span>
                </div>
                <strong>${(Number(item.price || 0) * item.quantity).toFixed(2)}</strong>
              </div>
            ))}
          </div>
        </aside>
      </div>
    </div>
  );
};

export default CheckoutPage;

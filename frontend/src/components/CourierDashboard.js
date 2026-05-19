import React, { useState, useEffect } from 'react';
import { Truck, CheckCircle, Clock } from 'lucide-react';
import api from '../api/axios'; // Використовуємо наш розумний axios
import './CourierDashboard.css';

const CourierDashboard = ({ onShowSuccessToast, onShowErrorToast }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  // 1. Завантаження замовлень, які очікують на доставку
  const fetchAvailableOrders = async () => {
    try {
      // Твій інтерцептор автоматично додасть токен з localStorage сюди!
      const response = await api.get('/orders/available');
      setOrders(response.data);
    } catch (error) {
      const backendMessage = error.response?.data || error.message;
      onShowErrorToast(
        typeof backendMessage === 'string' ? backendMessage : 'Failed to load delivery orders'
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAvailableOrders();
  }, []);

  // 2. Функція завершення доставки
  const handleDeliverOrder = async (orderId) => {
    try {
      await api.put(`/orders/${orderId}/deliver`);
      onShowSuccessToast('Order successfully marked as DELIVERED!');
      // Оновлюємо список, щоб доставлене замовлення зникло з екрана
      fetchAvailableOrders();
    } catch (error) {
      const backendMessage = error.response?.data || error.message;
      onShowErrorToast(
        typeof backendMessage === 'string' ? backendMessage : 'Failed to deliver order'
      );
    }
  };

  if (loading) return <div className="courier-loading">Loading delivery dashboard...</div>;

  return (
    <div className="courier-panel">
      <div className="courier-panel__header">
        <Truck size={32} className="courier-icon" />
        <h2>Courier Delivery Dashboard</h2>
      </div>

      {orders.length === 0 ? (
        <div className="courier-empty">
          <Clock size={48} className="empty-icon" />
          <p>No active orders to deliver right now. Good job!</p>
        </div>
      ) : (
        <div className="courier-panel__table-wrapper">
          <table className="courier-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Date & Time</th>
                <th>Customer</th>
                <th>Address</th>
                <th>Phone</th>
                <th>Order Details</th>
                <th>Total Price</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="order-id-cell" title={order.id}>
                    {order.id.substring(0, 8)}...
                  </td>

                  {/* Вивід та форматування дати/часу */}
                  <td className="date-cell">
                    {order.createdAt
                      ? (() => {
                          const dateObj = new Date(order.createdAt);
                          // Форматуємо дату: ДД.ММ.РРРР
                          const date = dateObj.toLocaleDateString('uk-UA', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                          });
                          // Форматуємо час: ГГ:ХХ
                          const time = dateObj.toLocaleTimeString('uk-UA', {
                            hour: '2-digit',
                            minute: '2-digit',
                          });
                          return (
                            <div className="order-date-time">
                              <span className="order-date">{date}</span>
                              <span className="order-time">{time}</span>
                            </div>
                          );
                        })()
                      : 'N/A'}
                  </td>

                  {/* Виводимо ім'я клієнта */}
                  <td className="customer-cell">{order.clientName || 'Anonymous'}</td>
                  <td>{order.deliveryAddress}</td>
                  <td>{order.contactPhone}</td>
                  <td className="details-cell">
                    <div className="order-items-list">
                      {order.items &&
                        order.items.map((item, index) => (
                          <div key={index} className="order-item-line">
                            • <strong>{item.name}</strong> x{item.quantity}
                          </div>
                        ))}
                    </div>
                  </td>
                  <td className="price-cell">{order.totalPrice} ₴</td>
                  <td>
                    <button
                      onClick={() => handleDeliverOrder(order.id)}
                      className="btn-action btn-deliver"
                      title="Mark as delivered"
                    >
                      <CheckCircle size={18} />
                      Complete Delivery
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default CourierDashboard;

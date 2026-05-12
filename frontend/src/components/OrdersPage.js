import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import OrderCard from './OrderCard';
import { Package } from 'lucide-react';
import './OrdersPage.css';
import './DishCard.css';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        const resp = await api.get('/orders');
        setOrders(resp.data || []);
        setError(null);
      } catch (e) {
        console.error('Failed to load orders', e);
        setError('Could not load orders.');
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  if (loading) {
    return (
      <div className="orders-page__center">
        <div className="orders-page__loading">Loading orders...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="orders-page__center">
        <div className="orders-page__empty">
          <Package size={56} className="orders-page__empty-icon" />
          <h2>Unable to load orders</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <div className="orders-page__center">
        <div className="orders-page__empty">
          <Package size={56} className="orders-page__empty-icon" />
          <h2>No orders yet</h2>
          <p>Your order history will appear here.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="orders-page">
      <h2 className="orders-page__title">Order History</h2>
      <div className="orders-page__grid">
        {orders.map((order) => (
          <OrderCard key={order.id} order={order} />
        ))}
      </div>
    </div>
  );
};

export default OrdersPage;

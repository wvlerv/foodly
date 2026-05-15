import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import OrdersPage from './components/OrdersPage';
import mockDishes from './data/mockDishes.json';
import NutritionChart from './components/NutritionChart';
import Cart from './components/Cart';
import authService from './services/authService';
import AuthPage from './components/AuthPage';
import api from './api/axios';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const [cartItems, setCartItems] = useState([]);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  const triggerToast = (message) => {
    setToastMessage(message);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  const updateQuantity = (id, amount) => {
    setCartItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, quantity: item.quantity + amount } : item))
    );
  };

  const removeFromCart = (id) => {
    setCartItems((prev) => prev.filter((item) => item.id !== id));
  };

  const handleCheckout = async () => {
    const token = localStorage.getItem('token');

    if (!token || token === 'undefined' || token === 'null') {
      throw new Error('Please log in to place an order.');
    }

    if (cartItems.length === 0) {
      throw new Error('Your cart is empty.');
    }

    const payload = {
      items: cartItems.map((item) => ({
        dishId: item.id,
        quantity: item.quantity,
      })),
    };

    await api.post('/orders', payload);
    setCartItems([]);
    alert('Order placed successfully! You can view it in Orders.');
  };

  const addToCart = (dish) => {
    setCartItems((prevItems) => {
      const existingItem = prevItems.find((item) => item.id === dish.id);
      if (existingItem) {
        return prevItems.map((item) =>
          item.id === dish.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...prevItems, { ...dish, quantity: 1 }];
    });
    triggerToast(`${dish.name} added to cart!`);
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
  };

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
  };

  // Рахуємо кількість товарів для хедера
  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <Router>
      <div className="App">
        {/* Передаємо cartCount у хедер, щоб він відображав цифру */}
        <Header cartCount={cartCount} isAuthenticated={isAuthenticated} onLogout={handleLogout} />

        {showToast && <div className="toast-notification">{toastMessage}</div>}

        <main className="App__main">
          <Routes>
            {/* Головна сторінка - Меню */}
            <Route
              path="/menu"
              element={<MenuCatalog dishes={mockDishes} onAddToCart={addToCart} />}
            />

            <Route path="/login" element={<AuthPage onLoginSuccess={handleLoginSuccess} />} />

            {/* Улюблені страви */}
            <Route
              path="/favorites"
              element={
                <MenuCatalog dishes={mockDishes} onAddToCart={addToCart} showFavoritesOnly={true} />
              }
            />

            {/* Сторінка аналітики (Твій графік) */}
            <Route path="/stats" element={<NutritionChart />} />

            {/* Кошик */}
            <Route
              path="/cart"
              element={
                <Cart
                  items={cartItems}
                  onUpdateQuantity={updateQuantity}
                  onRemove={removeFromCart}
                  onCheckout={handleCheckout}
                />
              }
            />

            {/* Замовлення */}
            <Route path="/orders" element={<OrdersPage />} />

            {/* Редірект з порожньої адреси на меню */}
            <Route path="/" element={<Navigate to="/menu" replace />} />
          </Routes>
        </main>

        <Footer />
      </div>
    </Router>
  );
}

export default App;

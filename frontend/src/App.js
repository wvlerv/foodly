import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import OrdersPage from './components/OrdersPage';
import CheckoutPage from './components/CheckoutPage';
import UserProfile from './components/UserProfile';
import mockDishes from './data/mockDishes.json';
import NutritionChart from './components/NutritionChart';
import Cart from './components/Cart';
import authService from './services/authService';
import AuthPage from './components/AuthPage';
import { initInactivityTracking } from './utils/inactivityTimeout';

function AppContent() {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const [cartItems, setCartItems] = useState([]);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastType, setToastType] = useState('success'); // 'success' or 'error'
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      initInactivityTracking();
    }
  }, [isAuthenticated]);
  
  const triggerToast = (message) => {
    setToastMessage(message);
    setToastType('success');
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  const triggerErrorToast = (message) => {
    setToastMessage(message);
    setToastType('error');
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
    setShowLogoutConfirm(true);
  };

  const handleCancelLogout = () => {
    setShowLogoutConfirm(false);
  };

  const handleConfirmLogout = async () => {
    setShowLogoutConfirm(false);
    await authService.logout();
    setIsAuthenticated(false);
    setCartItems([]);
    triggerToast('Logged out successfully!');
    navigate('/login', { replace: true });
  };

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
  };

  // Рахуємо кількість товарів для хедера
  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div className="App">
      {/* Передаємо cartCount у хедер, щоб він відображав цифру */}
      <Header cartCount={cartCount} isAuthenticated={isAuthenticated} onLogout={handleLogout} />

      {showToast && (
        <div className={`toast-notification toast-notification--${toastType}`}>{toastMessage}</div>
      )}

      {showLogoutConfirm && (
        <div className="logout-modal-overlay" role="presentation">
          <div
            className="logout-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="logout-confirm-title"
          >
            <h2 id="logout-confirm-title">Log out?</h2>
            <p>Are you sure you want to log out from your account?</p>
            <div className="logout-modal__actions">
              <button type="button" className="logout-modal__cancel" onClick={handleCancelLogout}>
                Cancel
              </button>
              <button type="button" className="logout-modal__confirm" onClick={handleConfirmLogout}>
                Log out
              </button>
            </div>
          </div>
        </div>
      )}

      <main className="App__main">
        <Routes>
          {/* Головна сторінка - Меню */}
          <Route
            path="/menu"
            element={
              <MenuCatalog
                dishes={mockDishes}
                onAddToCart={addToCart}
                onShowErrorToast={triggerErrorToast}
              />
            }
          />

          <Route
            path="/login"
            element={
              <AuthPage onLoginSuccess={handleLoginSuccess} onShowErrorToast={triggerErrorToast} />
            }
          />

          {/* Улюблені страви */}
          <Route
            path="/favorites"
            element={
              <MenuCatalog
                dishes={mockDishes}
                onAddToCart={addToCart}
                showFavoritesOnly={true}
                onShowErrorToast={triggerErrorToast}
              />
            }
          />

          {/* Сторінка аналітики (Твій графік) */}
          <Route path="/stats" element={<NutritionChart />} />

          <Route
            path="/profile"
            element={
              <UserProfile onShowSuccessToast={triggerToast} onShowErrorToast={triggerErrorToast} />
            }
          />

          {/* Кошик */}
          <Route
            path="/cart"
            element={
              <Cart
                items={cartItems}
                onUpdateQuantity={updateQuantity}
                onRemove={removeFromCart}
                onShowErrorToast={triggerErrorToast}
              />
            }
          />

          <Route
            path="/checkout"
            element={
              <CheckoutPage
                items={cartItems}
                onClearCart={() => setCartItems([])}
                onShowSuccessToast={triggerToast}
                onShowErrorToast={triggerErrorToast}
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
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;

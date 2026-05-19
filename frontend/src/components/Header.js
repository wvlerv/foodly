import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Utensils, ShoppingCart, User, Heart as HeartIcon, Truck } from 'lucide-react'; // Додали іконку Truck
import './Header.css';
import { LogOut, LogIn } from 'lucide-react';

/**
 * Header Component - Navigation and branding for Foodly
 */
const Header = ({ cartCount, isAuthenticated, userRole, onLogout }) => {
  const location = useLocation();
  const getActiveClass = (path) => (location.pathname === path ? 'active' : '');

  // Визначаємо, чи поточний користувач є кур'єром
  const isCourier = isAuthenticated && userRole === 'COURIER';

  return (
    <header className="header">
      <div className="header__container">
        {/* Logo - веде на головну (або на панель кур'єра, якщо це кур'єр) */}
        <Link to={isCourier ? '/courier' : '/menu'} className="header__logo">
          <Utensils className="logo-icon" size={28} />
          <h1>Foodly</h1>
        </Link>

        {/* Navigation Menu */}
        <nav className="header__nav">
          {isCourier ? (
            /* Вкладки СУТО ДЛЯ КУР'ЄРА */
            <Link to="/courier" className={`header__nav-link ${getActiveClass('/courier')}`}>
              <Truck size={18} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
              Delivery Dashboard
            </Link>
          ) : (
            /* Вкладки ДЛЯ КЛІЄНТІВ ТА АДМІНІВ */
            <>
              <Link to="/menu" className={`header__nav-link ${getActiveClass('/menu')}`}>
                Menu
              </Link>
              <Link to="/orders" className={`header__nav-link ${getActiveClass('/orders')}`}>
                Orders
              </Link>
              <Link to="/stats" className={`header__nav-link ${getActiveClass('/stats')}`}>
                Analytics
              </Link>
              {isAuthenticated && userRole === 'ADMIN' && (
                <Link
                  to="/admin"
                  className={`header__nav-link admin-link ${getActiveClass('/admin')}`}
                >
                  Admin Panel
                </Link>
              )}
            </>
          )}
        </nav>

        {/* Right Icons */}
        <div className="header__actions">
          {/* Показуємо Кошик та Улюблене ТІЛЬКИ якщо користувач НЕ кур'єр */}
          {!isCourier && (
            <>
              <Link
                to="/favorites"
                className={`header__icon-button ${getActiveClass('/favorites')}`}
                title="Favorites"
              >
                <HeartIcon size={24} />
              </Link>

              <Link
                to="/cart"
                className={`header__cart-link ${location.pathname === '/cart' ? 'active' : ''}`}
              >
                <ShoppingCart size={24} />
                {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
              </Link>
            </>
          )}

          {/* Профіль та Авторизація залишаються для всіх ролей */}
          <Link
            to={isAuthenticated ? '/profile' : '/login'}
            className={`header__icon-button ${getActiveClass('/profile')}`}
            title="User Profile"
          >
            <User size={24} />
          </Link>

          {isAuthenticated ? (
            <button className="header__icon-button logout-btn" onClick={onLogout} title="Logout">
              <LogOut size={24} />
              <span className="auth-text">Log Out</span>
            </button>
          ) : (
            <Link
              to="/login"
              className={`header__icon-button login-link ${getActiveClass('/login')}`}
              title="Login"
            >
              <LogIn size={24} />
              <span className="auth-text">Log In</span>
            </Link>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;

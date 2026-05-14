import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Utensils, ShoppingCart, User, Heart as HeartIcon } from 'lucide-react';
import './Header.css';

/**
 * Header Component - Navigation and branding for Foodly
 */
const Header = ({ cartCount }) => {
  const location = useLocation();

  // Функція для перевірки активного посилання
  const getActiveClass = (path) => (location.pathname === path ? 'active' : '');

  return (
    <header className="header">
      <div className="header__container">
        {/* Logo - веде на головну */}
        <Link to="/menu" className="header__logo">
          <Utensils className="logo-icon" size={28} />
          <h1>Foodly</h1>
        </Link>

        {/* Navigation Menu */}
        <nav className="header__nav">
          <Link to="/menu" className={`header__nav-link ${getActiveClass('/menu')}`}>
            Menu
          </Link>
          <Link to="/orders" className={`header__nav-link ${getActiveClass('/orders')}`}>
            Orders
          </Link>
          <Link to="/stats" className={`header__nav-link ${getActiveClass('/stats')}`}>
            Analytics
          </Link>
        </nav>

        {/* Right Icons */}
        <div className="header__actions">
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

          <button className="header__icon-button" title="User Profile">
            <User size={24} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;

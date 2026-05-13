import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Utensils, ShoppingCart, User, Heart as HeartIcon } from 'lucide-react';
import './Header.css';

/**
 * Header Component - Navigation and branding for Foodly
 *
 * Features:
 * - Foodly logo on the left with Lucide icon
 * - Navigation menu (Menu, Orders)
 * - Favorites, Cart and User Profile icons on the right
 * - Responsive design
 */
const Header = () => {
  const location = useLocation();

  return (
    <header className="header">
      <div className="header__container">
        {/* Logo */}
        <div className="header__logo">
          <Utensils className="logo-icon" size={28} />
          <h1>Foodly</h1>
        </div>

        {/* Navigation Menu */}
        <nav className="header__nav">
          <Link
            to="/menu"
            className={`header__nav-link ${location.pathname === '/menu' ? 'active' : ''}`}
          >
            Menu
          </Link>
          <Link
            to="/orders"
            className={`header__nav-link ${location.pathname === '/orders' ? 'active' : ''}`}
          >
            Orders
          </Link>
        </nav>

        {/* Right Icons */}
        <div className="header__actions">
          <Link
            to="/favorites"
            className="header__icon-button header__icon-button--link"
            title="Favorites"
          >
            <HeartIcon size={24} />
          </Link>
          <button className="header__icon-button" title="Cart">
            <ShoppingCart size={24} />
          </button>
          <button className="header__icon-button" title="User Profile">
            <User size={24} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;

import React from 'react';
import { Utensils, ShoppingCart, User } from 'lucide-react';
import './Header.css';

/**
 * Header Component - Navigation and branding for Foodly
 *
 * Features:
 * - Foodly logo on the left with Lucide icon
 * - Navigation menu (Menu, Orders)
 * - Cart and User Profile icons on the right
 * - Responsive design
 */
const Header = ({ onNavigate, currentPage }) => {
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
          <a
            href="#menu"
            className={`header__nav-link ${currentPage === 'menu' ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              onNavigate && onNavigate('menu');
            }}
          >
            Menu
          </a>
          <a
            href="#orders"
            className={`header__nav-link ${currentPage === 'orders' ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              onNavigate && onNavigate('orders');
            }}
          >
            Orders
          </a>
        </nav>

        {/* Right Icons */}
        <div className="header__actions">
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

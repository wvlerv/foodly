import React from 'react';
import { Mail, Camera, Send } from 'lucide-react';
import './Footer.css';

/**
 * Footer Component - Simple footer with copyright and social links
 */
const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer__container">
        <div className="footer__content">
          <p className="footer__copyright">&copy; 2026 Foodly. All rights reserved.</p>

          <div className="footer__social">
            <a href="mailto:support@foodly.com" className="footer__social-link" title="Email">
              <Mail size={20} />
            </a>

            {/* Telegram */}
            <a href="#telegram" className="footer__social-link" title="Telegram">
              <Send size={20} />
            </a>

            {/* Instagram */}
            <a href="#instagram" className="footer__social-link" title="Instagram">
              <Camera size={24} />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

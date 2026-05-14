import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './AuthPage.css';

const AuthPage = ({ onLoginSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const validate = () => {
    const newErrors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (!isLogin && formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
    if (serverError) setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    if (!validate()) return;

    setLoading(true);

    try {
      if (isLogin) {
        await authService.login(formData.email, formData.password);
        onLoginSuccess();
        navigate('/menu');
      } else {
        await authService.register(formData.email, formData.password);
        alert('Registration successful! You can now log in.');
        setIsLogin(true);
        setFormData((prev) => ({ ...prev, password: '', confirmPassword: '' }));
      }
    } catch (err) {
      const backendMessage = err.response?.data?.message || err.response?.data;

      if (
        err.response?.status === 409 ||
        (typeof backendMessage === 'string' && backendMessage.includes('exists'))
      ) {
        setServerError('This email is already registered. Please try logging in.');
      } else if (err.response?.status === 401) {
        setServerError('Invalid email or password. Please try again.');
      } else {
        setServerError('Server is unreachable. Please check your connection or try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setErrors({});
    setServerError('');
    setFormData({ email: '', password: '', confirmPassword: '' });
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>{isLogin ? 'Login to Foodly' : 'Join Foodly'}</h2>
        <p className="auth-subtitle">
          {isLogin
            ? 'Enter your details to access your account'
            : 'Start your healthy journey today'}
        </p>

        {serverError && <div className="auth-error-banner">{serverError}</div>}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-group">
            <label>Email Address</label>
            <input
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="name@example.com"
              className={errors.email ? 'input-error' : ''}
            />
            {errors.email && <span className="error-text">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Minimum 6 characters"
              className={errors.password ? 'input-error' : ''}
            />
            {errors.password && <span className="error-text">{errors.password}</span>}
          </div>

          {!isLogin && (
            <div className="form-group">
              <label>Confirm Password</label>
              <input
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Repeat your password"
                className={errors.confirmPassword ? 'input-error' : ''}
              />
              {errors.confirmPassword && (
                <span className="error-text">{errors.confirmPassword}</span>
              )}
            </div>
          )}

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? 'Processing...' : isLogin ? 'Log In' : 'Create Account'}
          </button>
        </form>

        <div className="auth-toggle">
          <span>{isLogin ? "Don't have an account?" : 'Already have an account?'}</span>
          <button type="button" onClick={toggleMode} className="toggle-link">
            {isLogin ? 'Sign Up' : 'Log In'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;

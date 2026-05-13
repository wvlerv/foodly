import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import OrdersPage from './components/OrdersPage';
import mockDishes from './data/mockDishes.json';

function App() {
  return (
    <Router>
      <div className="App">
        <Header />
        <main className="App__main">
          <Routes>
            <Route path="/menu" element={<MenuCatalog dishes={mockDishes} />} />
            <Route
              path="/favorites"
              element={<MenuCatalog dishes={mockDishes} showFavoritesOnly={true} />}
            />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/" element={<Navigate to="/menu" replace />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;

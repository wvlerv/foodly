import React, { useState } from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import OrdersPage from './components/OrdersPage';
import mockDishes from './data/mockDishes.json';
import NutritionChart from './components/NutritionChart';
import Cart from './components/Cart';

function App() {
  const [page, setPage] = useState('menu');
  const [cartItems, setCartItems] = useState([]);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  const triggerToast = (message) => {
    setToastMessage(message);
    setShowToast(true);
    // Ховаємо через 3 секунди
    setTimeout(() => setShowToast(false), 3000);
  };

  // Зміна кількості (+ або -)
  const updateQuantity = (id, amount) => {
    setCartItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, quantity: item.quantity + amount } : item))
    );
  };

  // Видалення з кошика
  const removeFromCart = (id) => {
    setCartItems((prev) => prev.filter((item) => item.id !== id));
  };

  // Імітація оформлення (тут дані мають йти на бекенд)
  const handleCheckout = () => {
    alert('Order placed successfully! Your analytics will update shortly.');
    setCartItems([]); // Очищуємо кошик
    setPage('stats'); // Повертаємось на графік
  };

  // Функція для додавання в кошик
  const addToCart = (dish) => {
    setCartItems((prevItems) => {
      // Перевіряємо, чи є вже така страва в кошику
      const existingItem = prevItems.find((item) => item.id === dish.id);
      if (existingItem) {
        // Якщо є — збільшуємо кількість
        return prevItems.map((item) =>
          item.id === dish.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      // Якщо немає — додаємо нову з кількістю 1
      return [...prevItems, { ...dish, quantity: 1 }];
    });
    triggerToast(`${dish.name} added to cart!`);
  };

  return (
    <div className="App">
      <Header
        onNavigate={setPage}
        currentPage={page}
        cartCount={cartItems.reduce((sum, item) => sum + item.quantity, 0)}
      />

      {showToast && <div className="toast-notification">{toastMessage}</div>}

      <main className="App__main">
        {page === 'stats' && (
          <div className="chart-container">
            <NutritionChart />
          </div>
        )}

        {page === 'menu' && <MenuCatalog dishes={mockDishes} onAddToCart={addToCart} />}

        {page === 'orders' && <OrdersPage />}

        {page === 'cart' && (
          <Cart
            items={cartItems}
            onUpdateQuantity={updateQuantity}
            onRemove={removeFromCart}
            onCheckout={handleCheckout}
            onNavigate={setPage}
          />
        )}
      </main>
      <Footer />
    </div>
  );
}

export default App;

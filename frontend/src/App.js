import React from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import OrdersPage from './components/OrdersPage';
import mockDishes from './data/mockDishes.json';
import NutritionChart from './components/NutritionChart';

function App() {
  const [page, setPage] = useState('menu');

  return (
    <div className="App">
      <Header onNavigate={setPage} currentPage={page} />
      <main className="App__main">
        <div className="chart-container">
          <NutritionChart />
        </div>

        <MenuCatalog dishes={mockDishes} />
        {page === 'menu' && <MenuCatalog dishes={mockDishes} />}
        {page === 'orders' && <OrdersPage />}
      </main>
      <Footer />
    </div>
  );
}

export default App;

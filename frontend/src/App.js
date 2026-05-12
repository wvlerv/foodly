import React from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import mockDishes from './data/mockDishes.json';
import NutritionChart from './components/NutritionChart';

function App() {
  return (
    <div className="App">
      <Header />
      <main className="App__main">
        <div className="chart-container">
          <NutritionChart />
        </div>

        <MenuCatalog dishes={mockDishes} />
      </main>
      <Footer />
    </div>
  );
}

export default App;

import React from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import MenuCatalog from './components/MenuCatalog';
import mockDishes from './data/mockDishes.json';

function App() {
  return (
    <div className="App">
      <Header />
      <main className="App__main">
        <MenuCatalog dishes={mockDishes} />
      </main>
      <Footer />
    </div>
  );
}

export default App;

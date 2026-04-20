import './App.css';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Sidebar from './components/sidebar'; 
import Principal from './pages/principal';
import Usuarios from './pages/usuarios';
import Analitica from './pages/analitica';

function App() {
  return (
    <BrowserRouter>
      {/* Contenedor principal para organizar el layout */}
      <div className="app-layout">
        
        {/* El Sidebar se queda fijo aquí */}
        <Sidebar />

        {/* El contenido de la derecha cambia según la URL */}
        <main className="main-content">
          <Routes>
            {/* 🟢 CORREGIDO: Eliminada la coma después de Principal */}
            <Route path="/" element={<Principal />} />
            <Route path='/usuarios' element={<Usuarios/>} />
            <Route path='/analitica' element={<Analitica/>} />
          </Routes>
        </main>

      </div>
    </BrowserRouter>
  );
}

export default App;
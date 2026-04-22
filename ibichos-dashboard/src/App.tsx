import './App.css';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from './context/authcontext'; //  Importamos el proveedor
import { ProtectedRoute } from './components/protectedRoute'; //  Importamos el protector
import Sidebar from './components/sidebar'; 
import Principal from './pages/principal';
import Analitica from './pages/analitica';
import { Login } from './pages/login';
import Usuarios from './pages/usuarios';

function App() {
  return (
    <AuthProvider> {/* Envolvemos toda la app con el contexto */}
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />

     
           {/* Envolvemos todas las rutas privadas con ProtectedRoute */}
          <Route path="/*" element={
            <ProtectedRoute> 
              {/*  1. Contenedor Padre: Toma el 100% de la pantalla y oculta el scroll general */}
              <div className="d-flex vh-100 w-100 overflow-hidden">
                
                <Sidebar />

                {/* 2. Contenedor Principal: Ocupa el espacio restante y tiene su propio scroll */}
                <main className="flex-grow-1 overflow-auto bg-light p-4">
                  <Routes>
                    <Route path="principal" element={<Principal />} />
                    <Route path="usuarios" element={<Usuarios />} />
                    <Route path="analitica" element={<Analitica />} />
                    <Route path="*" element={<Navigate to="/principal" />} />
                  </Routes>
                </main>
                
              </div>
            </ProtectedRoute>
          } />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
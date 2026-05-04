import './App.css';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from './context/authcontext';
import { ProtectedRoute } from './components/protectedRoute';
import Sidebar from './components/sidebar'; 
import Principal from './pages/principal';
import Analitica from './pages/analitica';
import { Login } from './pages/login';
import Usuarios from './pages/usuarios';
import Capturas from './pages/capturas';
import Geografia from './pages/geografia';
import Catalogo from './pages/catalogo';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />

          <Route path="/*" element={
            <ProtectedRoute> 
              <div className="d-flex flex-column flex-lg-row min-vh-100 bg-light">
                <Sidebar />
                <main className="flex-grow-1 p-3 p-lg-4" style={{ overflowY: 'auto' }}>
                  <Routes>
                    <Route path="principal" element={<Principal />} />
                    <Route path="usuarios" element={<Usuarios />} />
                    <Route path="analitica" element={<Analitica />} />
                    <Route path="geografia" element={<Geografia />} />
                    <Route path="catalogo" element={<Catalogo />} />
                    <Route path="capturas" element={<Capturas />} />
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
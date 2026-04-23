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

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />

          <Route path="/*" element={
            <ProtectedRoute> 
              <div className="d-flex vh-100 w-100 overflow-hidden">
                <Sidebar />
                <main className="flex-grow-1 overflow-auto bg-light p-4">
                  <Routes>
                    <Route path="principal" element={<Principal />} />
                    <Route path="usuarios" element={<Usuarios />} />
                    <Route path="analitica" element={<Analitica />} />
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
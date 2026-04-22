import { Link, useNavigate, useLocation } from 'react-router-dom';
import { signOut } from 'firebase/auth';
import { auth } from '../config/firebaseConfig';
import { useAuth } from '../context/authcontext'; 

export default function Sidebar() {
  const { user } = useAuth(); 
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try {
      await signOut(auth);
      navigate('/'); 
    } catch (error) {
      console.error("Error al cerrar sesión", error);
    }
  };

  return (
    <aside className="d-flex flex-column flex-shrink-0 p-3 text-bg-dark vh-100" style={{ width: '280px' }}>
      
      {/* Título / Logo */}
      <Link to="/principal" className="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
        <span className="fs-4 fw-bold">Mi Panel Web</span>
      </Link>
      
      <hr />
      
      {/* Navegación usando nav-pills de Bootstrap */}
      <ul className="nav nav-pills flex-column mb-auto gap-1">
        <li className="nav-item">
          <Link 
            to="/principal" 
            className={`nav-link text-white ${location.pathname === '/principal' ? 'active' : ''}`}
          >
            Principal
          </Link>
        </li>
        <li className="nav-item">
          <Link 
            to="/usuarios" 
            className={`nav-link text-white ${location.pathname === '/usuarios' ? 'active' : ''}`}
          >
            Usuarios
          </Link>
        </li>
        <li className="nav-item">
          <Link 
            to="/analitica" 
            className={`nav-link text-white ${location.pathname === '/analitica' ? 'active' : ''}`}
          >
            Analítica
          </Link>
        </li>
      </ul>

      <hr />

      {/* Footer del Sidebar: Usuario y Botón */}
      <div className="mt-auto">
        <div className="d-flex flex-column gap-3">
          
          <div className="small text-truncate" title={user?.email || ''}>
            <span className="text-white-50">Logueado como:</span><br/>
            <strong>{user?.email}</strong>
          </div>
          
          <button 
            onClick={handleLogout} 
            className="btn btn-danger w-100 fw-semibold"
          >
            Cerrar Sesión
          </button>
          
        </div>
      </div>
    </aside>
  );
}
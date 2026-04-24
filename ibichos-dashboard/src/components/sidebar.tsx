import { Link, useNavigate, useLocation } from 'react-router-dom';
import { signOut } from 'firebase/auth';
import { auth } from '../config/firebaseConfig';
import { useAuth } from '../context/authcontext'; 

export default function Sidebar() {
  const { user, username } = useAuth(); 
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
    <aside className="d-flex flex-column flex-shrink-0 p-3 text-white vh-100 sidebar-ibichos" style={{ width: '280px', background: 'linear-gradient(180deg, #1B5E20 0%, #2E7D32 100%)', boxShadow: '4px 0 15px rgba(0,0,0,0.1)' }}>
      
      {/* Título / Logo */}
      <Link to="/principal" className="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
        <img src="/logo_oficial.png" alt="iBichos" style={{ width: '45px', marginRight: '12px', filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.3))' }}/>
        <span className="fs-4 fw-bolder" style={{ letterSpacing: '-0.5px' }}>iBichos Admin</span>
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
            <span className="text-white-50">Administrador:</span><br/>
            <strong className="fs-6 text-warning">{username}</strong>
          </div>
          
          <button 
            onClick={handleLogout} 
            className="btn w-100 fw-bold border-0 shadow-sm"
            style={{ backgroundColor: 'rgba(255,255,255,0.15)', color: '#fff', transition: 'all 0.2s' }}
            onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#d32f2f'; }}
            onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.15)'; }}
          >
            Cerrar Sesión
          </button>
          
        </div>
      </div>
    </aside>
  );
}
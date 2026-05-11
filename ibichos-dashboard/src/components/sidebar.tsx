import { Link, useNavigate, useLocation } from 'react-router-dom';
import { signOut } from 'firebase/auth';
import { auth } from '../config/firebaseConfig';
import { useAuth } from '../context/authcontext';
// Importamos los iconos de Lucide (asumiendo que los tienes instalados por tu código anterior)
import { LogOut, Menu, User, Activity, Users, Home, Globe2, Bug, ShieldAlert } from 'lucide-react';
import { useState } from 'react';

export default function Sidebar() {
  const { user, username } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isNavOpen, setIsNavOpen] = useState(false); // Estado para el menú móvil

  const handleLogout = async () => {
    try {
      await signOut(auth);
      navigate('/');
    } catch (error) {
      console.error("Error al cerrar sesión", error);
    }
  };

  const closeNav = () => setIsNavOpen(false);

  // Colores base definidos en tu diseño
  const bgStyle = {
    background: 'linear-gradient(180deg, #1B5E20 0%, #2E7D32 100%)',
    boxShadow: '4px 0 15px rgba(0,0,0,0.1)'
  };

  // Clases comunes para los enlaces activos/inactivos
  const getLinkClasses = (path: string) => {
    const isActive = location.pathname === path;
    return `nav-link text-white rounded-3 px-3 py-2 fw-semibold d-flex align-items-center gap-2 ${
      isActive ? 'bg-success bg-opacity-25 border-start border-4 border-white' : 'hover-bg-opacity'
    }`;
  };

  return (
    <>
      {/* -------------------------------------------------------------
          VERSIÓN MOBILE / TABLET (TOP HEADER)
          Visible solo en pantallas menores a lg (lg = 992px)
      ------------------------------------------------------------- */}
      <nav className="navbar navbar-dark d-lg-none sticky-top shadow-sm px-3 py-2" style={bgStyle}>
        <div className="container-fluid p-0">
          {/* Logo y Título Móvil */}
          <Link to="/principal" className="navbar-brand d-flex align-items-center m-0" onClick={closeNav}>
            <img src="/logo_oficial.png" alt="iBichos" style={{ width: '32px', marginRight: '10px', filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.3))' }} />
            <span className="fs-5 fw-bolder" style={{ letterSpacing: '-0.5px' }}>iBichos Admin</span>
          </Link>

          {/* Botón Hamburguesa */}
          <button 
            className="navbar-toggler border-0 px-2" 
            type="button" 
            onClick={() => setIsNavOpen(!isNavOpen)}
            aria-controls="mobileNavbar" 
            aria-expanded={isNavOpen} 
            aria-label="Toggle navigation"
          >
            <Menu size={28} />
          </button>

          {/* Contenido Colapsable del Menú Móvil */}
          <div className={`collapse navbar-collapse ${isNavOpen ? 'show' : ''} mt-3`} id="mobileNavbar">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0 gap-2">
              <li className="nav-item">
                <Link to="/principal" className={getLinkClasses('/principal')} onClick={closeNav}>
                  <Home size={18} /> Principal
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/usuarios" className={getLinkClasses('/usuarios')} onClick={closeNav}>
                  <Users size={18} /> Usuarios
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/analitica" className={getLinkClasses('/analitica')} onClick={closeNav}>
                  <Activity size={18} /> Analítica
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/geografia" className={getLinkClasses('/geografia')} onClick={closeNav}>
                  <Globe2 size={18} /> Geografía
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/catalogo" className={getLinkClasses('/catalogo')} onClick={closeNav}>
                  <Bug size={18} /> Catálogo
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/capturas" className={getLinkClasses('/capturas')} onClick={closeNav}>
                  <ShieldAlert size={18} /> Moderación
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/auditoria" className={getLinkClasses('/auditoria')} onClick={closeNav}>
                  <ShieldAlert size={18} className="text-warning" /> Auditoría
                </Link>
              </li>
            </ul>

            <hr className="text-white-50 my-3" />

            {/* Info Usuario y Logout Móvil */}
            <div className="d-flex flex-column gap-3 pb-2">
              <div className="d-flex align-items-center gap-2 text-white">
                <div className="bg-white bg-opacity-25 p-2 rounded-circle">
                  <User size={18} />
                </div>
                <div className="small text-truncate">
                  <span className="text-white-50 d-block" style={{ fontSize: '0.75rem' }}>Administrador</span>
                  <strong className="text-warning">{username}</strong>
                </div>
              </div>
              
              <button onClick={handleLogout} className="btn btn-outline-light w-100 fw-bold d-flex align-items-center justify-content-center gap-2">
                <LogOut size={18} /> Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* -------------------------------------------------------------
          VERSIÓN DESKTOP (SIDEBAR LATERAL CLÁSICO)
          Visible solo en pantallas desde lg en adelante (lg = 992px)
      ------------------------------------------------------------- */}
      <aside className="d-none d-lg-flex flex-column flex-shrink-0 p-3 text-white vh-100 position-sticky top-0 sidebar-ibichos" style={{ width: '280px', ...bgStyle }}>
        
        {/* Título / Logo Desktop */}
        <Link to="/principal" className="d-flex align-items-center mb-4 text-white text-decoration-none px-2">
          <img src="/logo_oficial.png" alt="iBichos" style={{ width: '45px', marginRight: '12px', filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.3))' }}/>
          <span className="fs-4 fw-bolder" style={{ letterSpacing: '-0.5px' }}>iBichos Admin</span>
        </Link>
        
        {/* Navegación Desktop */}
        <ul className="nav nav-pills flex-column mb-auto gap-2 px-2">
          <li className="nav-item">
            <Link to="/principal" className={getLinkClasses('/principal')}>
              <Home size={20} /> Principal
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/usuarios" className={getLinkClasses('/usuarios')}>
              <Users size={20} /> Usuarios
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/analitica" className={getLinkClasses('/analitica')}>
              <Activity size={20} /> Analítica
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/geografia" className={getLinkClasses('/geografia')}>
              <Globe2 size={20} /> Geografía
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/catalogo" className={getLinkClasses('/catalogo')}>
              <Bug size={20} /> Catálogo
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/capturas" className={getLinkClasses('/capturas')}>
              <ShieldAlert size={20} /> Moderación
            </Link>
          </li>
          <li className="nav-item">
            <Link to="/auditoria" className={getLinkClasses('/auditoria')}>
              <ShieldAlert size={20} className="text-warning" /> Auditoría
            </Link>
          </li>
        </ul>

        {/* Footer del Sidebar Desktop: Usuario y Botón */}
        <div className="mt-auto px-2">
          <hr className="text-white-50 border-2" />
          <div className="d-flex flex-column gap-3 pt-2">
            
            <div className="d-flex align-items-center gap-3">
              <div className="bg-white bg-opacity-25 p-2 rounded-circle flex-shrink-0">
                <User size={24} className="text-white" />
              </div>
              <div className="small text-truncate" title={user?.email || ''}>
                <span className="text-white-50 d-block mb-1" style={{ fontSize: '0.8rem' }}>Administrador</span>
                <strong className="fs-6 text-warning d-block text-truncate">{username}</strong>
              </div>
            </div>
            
            <button 
              onClick={handleLogout} 
              className="btn w-100 fw-bold border-0 shadow-sm mt-2 d-flex align-items-center justify-content-center gap-2"
              style={{ backgroundColor: 'rgba(255,255,255,0.15)', color: '#fff', transition: 'all 0.2s' }}
              onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#dc3545'; }}
              onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.15)'; }}
            >
              <LogOut size={18} /> Cerrar Sesión
            </button>
            
          </div>
        </div>
      </aside>
    </>
  );
}
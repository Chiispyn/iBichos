import { 
  LayoutDashboard, 
  Users, 
  BarChart3,   
} from "lucide-react";
// 1. IMPORTANTE: Importar NavLink
import { NavLink } from "react-router-dom";

const Sidebar = () => {
  const menuItems = [
    { name: "Dashboard", icon: <LayoutDashboard />, path: "/" },
    { name: "Comunidad y Usuarios", icon: <Users />, path: "/usuarios" },
    { name: "Analítica y KPIs", icon: <BarChart3 />, path: "/analitica" },
  ];

  return (
    <aside className="sidebar">
      {/* SECCIÓN SUPERIOR: LOGO */}
      <div className="sidebar-header">
        <div className="logo-container">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="8" />
            <path d="M12 2v2M12 20v2M2 12h2M20 12h2" />
          </svg>
        </div>
        <div className="brand-info">
          <h1>iBichos</h1>
          <p>Panel Admin</p>
        </div>
      </div>

      {/* SECCIÓN CENTRAL: NAVEGACIÓN */}
      <nav className="sidebar-nav">
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {menuItems.map((item, index) => (
            <li key={index}>
              {/* 2. CAMBIO: button por NavLink */}
              <NavLink 
                to={item.path} 
                className={({ isActive }: { isActive: boolean }) => isActive ? "nav-link active" : "nav-link"}
                style={{ textDecoration: 'none', color: 'inherit' }}
              >
                <span className="icon">{item.icon}</span>
                <span className="label">{item.name}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      {/* SECCIÓN INFERIOR: PERFIL */}
      <footer className="sidebar-footer">
        <div className="user-avatar">AD</div>
        <div className="user-details">
          <p className="user-name">Admin User</p>
          <p className="user-email">admin@ibichos.com</p>
        </div>
      </footer>
    </aside>
  );
};

export default Sidebar;
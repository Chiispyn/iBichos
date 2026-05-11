import type { Usuario } from "../types/usuario";
import { useState, useMemo } from 'react';
import { doc, setDoc, deleteDoc, collection } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import { ChevronLeft, ChevronRight, ChevronDown, ChevronUp, Search, UserCircle2 } from 'lucide-react';
import { useAuth } from '../context/authcontext';

interface TablaUsuariosProps {
  usuariosFiltrados: Usuario[];
  adminsIds: string[];
}

const TablaUsuarios = ({ usuariosFiltrados, adminsIds }: TablaUsuariosProps) => {
  const { user } = useAuth();
  const [modal, setModal] = useState<{isOpen: boolean, title: string, message: string, onConfirm: (() => void) | null, confirmColor: string}>({
    isOpen: false, title: '', message: '', onConfirm: null, confirmColor: 'btn-primary'
  });

  // Estados para Filtros, Paginación y Orden
  const [filtroTab, setFiltroTab] = useState<'Todos' | 'Jugadores' | 'Moderadores' | 'Baneados'>('Todos');
  const [ordenColumna, setOrdenColumna] = useState<'username' | 'xp' | 'level'>('xp');
  const [ordenDireccion, setOrdenDireccion] = useState<'asc' | 'desc'>('desc');
  const [paginaActual, setPaginaActual] = useState(1);
  const [busqueda, setBusqueda] = useState('');
  const [filtroNivel, setFiltroNivel] = useState('Todos');
  const ITEMS_POR_PAGINA = 10;

  const showModal = (title: string, message: string, onConfirm: (() => void) | null, confirmColor: string = 'btn-primary') => {
    setModal({ isOpen: true, title, message, onConfirm, confirmColor });
  };

  const closeModal = () => setModal(prev => ({ ...prev, isOpen: false }));

  const logAudit = async (action: string, targetId: string) => {
    try {
      const logRef = doc(collection(db, 'moderation_logs'));
      await setDoc(logRef, {
        adminId: user?.uid || 'unknown',
        adminEmail: user?.email || 'Admin',
        action,
        targetId,
        targetType: 'USER',
        timestamp: new Date()
      });
    } catch (e) {
      console.warn("Error guardando audit log (faltan reglas?):", e);
    }
  };

  // --- LÓGICA DE FIREBASE ---
  const handleHacerAdmin = (usuario: Usuario) => {
    showModal(
      'Nombrar Administrador',
      `¿Estás seguro que deseas promover a ${usuario.username} (${usuario.email}) como Administrador?`,
      async () => {
        closeModal();
        try {
          const adminRef = doc(db, 'admins', usuario.id);
          await setDoc(adminRef, { email: usuario.email, status: 'active', role: 'admin' });
          await logAudit('MAKE_ADMIN', usuario.id);
          showModal('¡Éxito!', `${usuario.username} ahora es administrador. (Recarga la página para ver los cambios)`, null);
        } catch (error) {
          console.error("Error al promover a admin:", error);
          showModal('Error', 'Hubo un error al intentar hacerlo administrador. Revisa la consola.', null);
        }
      },
      'btn-primary'
    );
  };

  const handleQuitarAdmin = (usuario: Usuario) => {
    showModal(
      'Revocar Administrador',
      `⚠️ ¿Estás seguro que deseas QUITAR el acceso de Administrador a ${usuario.username}?`,
      async () => {
        closeModal();
        try {
          const adminRef = doc(db, 'admins', usuario.id);
          await deleteDoc(adminRef);
          await logAudit('REMOVE_ADMIN', usuario.id);
          showModal('¡Revocado!', `Se ha revocado el acceso a ${usuario.username}. (Recarga la página para ver los cambios)`, null);
        } catch (error) {
          console.error("Error al revocar admin:", error);
          showModal('Error', 'Hubo un error al intentar quitarle el acceso. Revisa la consola.', null);
        }
      },
      'btn-danger'
    );
  };

  const handleToggleBan = (usuario: Usuario) => {
    const isCurrentlyBanned = !!usuario.isShadowBanned;
    const accion = isCurrentlyBanned ? 'Desbanear' : 'Shadowbanear';
    const color = isCurrentlyBanned ? 'btn-success' : 'btn-dark';
    
    showModal(
      `${accion} Usuario`,
      `¿Estás seguro que deseas ${accion.toLowerCase()} a ${usuario.username}?`,
      async () => {
        closeModal();
        try {
          const userRef = doc(db, 'users', usuario.id);
          await setDoc(userRef, { isShadowBanned: !isCurrentlyBanned }, { merge: true });
          await logAudit(isCurrentlyBanned ? 'UNBAN_USER' : 'BAN_USER', usuario.id);
          showModal('¡Éxito!', `Se ha actualizado el estado de moderación de ${usuario.username}. (Recarga la página para ver los cambios)`, null);
        } catch (error) {
          console.error(`Error al ${accion}:`, error);
          showModal('Error', `Hubo un error al intentar ${accion}. Revisa la consola.`, null);
        }
      },
      color
    );
  };

  // --- LÓGICA DE VISTA (Tabs, Búsqueda, Orden, Paginación) ---
  const usuariosProcesados = useMemo(() => {
    let result = [...usuariosFiltrados];

    // 1. Filtrar por Tab
    if (filtroTab === 'Moderadores') {
      result = result.filter(u => adminsIds.includes(u.id));
    } else if (filtroTab === 'Baneados') {
      result = result.filter(u => u.isShadowBanned);
    } else if (filtroTab === 'Jugadores') {
      result = result.filter(u => !adminsIds.includes(u.id));
    }

    // 2. Filtrar por Nivel/Liga
    if (filtroNivel !== 'Todos') {
      result = result.filter(u => u.level && u.level.toUpperCase() === filtroNivel.toUpperCase());
    }

    // 3. Filtrar por Búsqueda
    if (busqueda.trim() !== '') {
      const b = busqueda.toLowerCase();
      result = result.filter(u => 
        u.username.toLowerCase().includes(b) || 
        u.email.toLowerCase().includes(b)
      );
    }

    // 4. Ordenamiento
    result.sort((a, b) => {
      let valA = a[ordenColumna] || '';
      let valB = b[ordenColumna] || '';
      
      // Si comparamos strings (como username) lo hacemos en minúscula
      if (typeof valA === 'string' && typeof valB === 'string') {
        valA = valA.toLowerCase();
        valB = valB.toLowerCase();
      }

      if (valA < valB) return ordenDireccion === 'asc' ? -1 : 1;
      if (valA > valB) return ordenDireccion === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  }, [usuariosFiltrados, adminsIds, filtroTab, filtroNivel, busqueda, ordenColumna, ordenDireccion]);

  // 4. Paginación
  const totalPaginas = Math.max(1, Math.ceil(usuariosProcesados.length / ITEMS_POR_PAGINA));
  // Corrección si la página actual supera el nuevo total (ej. al buscar o cambiar tab)
  const paginaSegura = Math.min(paginaActual, totalPaginas);
  const indexInicio = (paginaSegura - 1) * ITEMS_POR_PAGINA;
  const usuariosPaginados = usuariosProcesados.slice(indexInicio, indexInicio + ITEMS_POR_PAGINA);

  const toggleOrden = (columna: 'username' | 'xp' | 'level') => {
    if (ordenColumna === columna) {
      setOrdenDireccion(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setOrdenColumna(columna);
      setOrdenDireccion('desc'); // Por defecto descendente
    }
  };

  const getSortIcon = (columna: string) => {
    if (ordenColumna !== columna) return null;
    return ordenDireccion === 'asc' ? <ChevronUp size={16} className="ms-1" /> : <ChevronDown size={16} className="ms-1" />;
  };

  // UI Helpers
  const getAvatarColor = (name: string) => {
    const colors = ['bg-primary', 'bg-success', 'bg-danger', 'bg-warning', 'bg-info', 'bg-secondary', 'bg-dark'];
    const charCode = name.charCodeAt(0) || 0;
    return colors[charCode % colors.length];
  };

  return (
    <div>
      {/* Controles Superiores: Tabs, Filtros y Buscador */}
      <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-3">
        {/* Pestañas de Roles */}
        <div className="nav nav-pills p-1 bg-light rounded-pill border" style={{ width: 'fit-content' }}>
          {(['Todos', 'Jugadores', 'Moderadores', 'Baneados'] as const).map(tab => (
            <button 
              key={tab}
              className={`nav-link rounded-pill fw-bold px-4 py-2 ${filtroTab === tab ? 'active shadow-sm' : 'text-secondary'}`}
              onClick={() => { setFiltroTab(tab); setPaginaActual(1); }}
            >
              {tab}
            </button>
          ))}
        </div>

        <div className="d-flex flex-column flex-sm-row gap-2" style={{ width: '100%', maxWidth: '450px' }}>
          {/* Selector de Nivel */}
          <select 
            className="form-select rounded-pill border bg-white text-secondary shadow-none" 
            style={{ width: '100%', maxWidth: '160px' }}
            value={filtroNivel}
            onChange={(e) => { setFiltroNivel(e.target.value); setPaginaActual(1); }}
          >
            <option value="Todos">Todas las ligas</option>
            <option value="Casual">Casual</option>
            <option value="Amateur">Amateur</option>
            <option value="Explorador">Explorador</option>
            <option value="Entomólogo">Entomólogo</option>
            <option value="Maestro de Bichos">M. de Bichos</option>
          </select>

          {/* Buscador */}
          <div className="position-relative flex-grow-1">
            <Search className="position-absolute text-muted" size={18} style={{ left: '12px', top: '10px' }} />
            <input 
              type="text" 
              className="form-control rounded-pill ps-5 bg-white border shadow-none" 
              placeholder="Buscar nombre o correo..."
              value={busqueda}
              onChange={(e) => { setBusqueda(e.target.value); setPaginaActual(1); }}
            />
          </div>
        </div>
      </div>

      {/* Tabla Principal */}
      <div className="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th className="ps-4 text-secondary" style={{ cursor: 'pointer', width: '35%' }} onClick={() => toggleOrden('username')}>
                  <div className="d-flex align-items-center">Usuario {getSortIcon('username')}</div>
                </th>
                <th className="d-none d-md-table-cell text-secondary">Contacto & Ubicación</th>
                <th className="text-center text-secondary" style={{ cursor: 'pointer' }} onClick={() => toggleOrden('xp')}>
                  <div className="d-flex align-items-center justify-content-center">Progreso {getSortIcon('xp')}</div>
                </th>
                <th className="d-none d-md-table-cell text-secondary">Estado de Cuenta</th>
                <th className="text-center pe-4 text-secondary">Acciones</th>
              </tr>
            </thead>
            
            <tbody>
              {usuariosPaginados.length > 0 ? (
                usuariosPaginados.map((usuario) => (
                  <tr key={usuario.id} className="border-bottom">
                    
                    {/* Columna 1: Avatar y Nombre (Reemplaza al UID) */}
                    <td className="ps-4 py-3">
                      <div className="d-flex align-items-center gap-3">
                        <div className={`d-flex align-items-center justify-content-center text-white rounded-circle fw-bold fs-5 shadow-sm ${getAvatarColor(usuario.username)}`} style={{ width: '45px', height: '45px' }}>
                          {usuario.username.charAt(0).toUpperCase()}
                        </div>
                        <div className="d-flex flex-column">
                          <span className="fw-bolder text-dark fs-6">
                            {usuario.username}
                            {adminsIds.includes(usuario.id) && (
                              <span className="badge bg-danger ms-2 rounded-pill" style={{ fontSize: '0.65rem' }}>Admin</span>
                            )}
                          </span>
                          <small className="text-muted text-uppercase" style={{ fontSize: '0.7rem' }}>
                            Miembro desde {usuario.createdAt || 'N/A'}
                          </small>
                        </div>
                      </div>
                    </td>

                    {/* Columna 2: Email y Ubicación condensados */}
                    <td className="d-none d-md-table-cell">
                      <div className="d-flex flex-column">
                        <span className="text-dark mb-1">{usuario.email}</span>
                        <div className="d-flex align-items-center gap-2">
                          <span className="badge bg-light border text-secondary" style={{ fontSize: '0.65rem' }}>{usuario.region}</span>
                          <small className="text-muted text-truncate" style={{ maxWidth: '150px' }}>{usuario.comuna}</small>
                        </div>
                      </div>
                    </td>

                    {/* Columna 3: Progreso (Nivel y XP) */}
                    <td className="text-center">
                      <div className="d-flex flex-column align-items-center">
                        <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 rounded-pill px-3 mb-1">
                          {usuario.level}
                        </span>
                        <small className="text-muted fw-bold">{usuario.xp} XP</small>
                      </div>
                    </td>

                    {/* Columna 4: Estado y Reputación */}
                    <td className="d-none d-md-table-cell">
                      <div className="d-flex flex-column">
                        <span className={`badge ${usuario.isShadowBanned ? 'bg-danger' : 'bg-success'} align-self-start mb-1 rounded-pill`} style={{ fontSize: '0.7rem' }}>
                          {usuario.isShadowBanned ? 'CUENTA BANEADA' : 'ACTIVO'}
                        </span>
                        <small className="text-muted">
                          {usuario.strikes || 0} strike(s) registrados
                        </small>
                      </div>
                    </td>

                    {/* Columna 5: Acciones */}
                    <td className="text-center pe-4">
                      <div className="d-flex justify-content-center gap-2">
                        <button 
                          className={`btn btn-sm ${adminsIds.includes(usuario.id) ? 'btn-outline-danger' : 'btn-outline-primary'}`}
                          title={adminsIds.includes(usuario.id) ? "Quitar Administrador" : "Nombrar Administrador"}
                          onClick={() => adminsIds.includes(usuario.id) ? handleQuitarAdmin(usuario) : handleHacerAdmin(usuario)}
                          style={{ borderRadius: '0.5rem' }}
                        >
                          <i className={`bi ${adminsIds.includes(usuario.id) ? 'bi-shield-minus' : 'bi-shield-check'} me-1`}></i> 
                          {adminsIds.includes(usuario.id) ? 'Revocar' : 'Ascender'}
                        </button>
                        
                        <button 
                          className={`btn btn-sm ${usuario.isShadowBanned ? 'btn-success' : 'btn-dark'}`}
                          title={usuario.isShadowBanned ? "Levantar Shadowban" : "Aplicar Shadowban"}
                          onClick={() => handleToggleBan(usuario)}
                          style={{ borderRadius: '0.5rem' }}
                        >
                          <i className={`bi ${usuario.isShadowBanned ? 'bi-unlock' : 'bi-slash-circle'} me-1`}></i> 
                          {usuario.isShadowBanned ? 'Restaurar' : 'Banear'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="text-center py-5 text-muted">
                    <UserCircle2 size={48} className="text-secondary opacity-50 mb-3" />
                    <h5 className="fw-bold">No se encontraron resultados</h5>
                    <p className="mb-0">Prueba cambiando los filtros o la búsqueda.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        {/* Controles de Paginación */}
        {totalPaginas > 1 && (
          <div className="card-footer bg-white border-top py-3 px-4 d-flex justify-content-between align-items-center">
            <span className="text-muted small">
              Mostrando <strong>{indexInicio + 1}</strong> a <strong>{Math.min(indexInicio + ITEMS_POR_PAGINA, usuariosProcesados.length)}</strong> de <strong>{usuariosProcesados.length}</strong> usuarios
            </span>
            <div className="d-flex gap-1">
              <button 
                className="btn btn-sm btn-outline-secondary"
                disabled={paginaSegura === 1}
                onClick={() => setPaginaActual(p => Math.max(1, p - 1))}
              >
                <ChevronLeft size={16} />
              </button>
              
              <div className="d-flex align-items-center px-3 fw-bold text-secondary">
                {paginaSegura} / {totalPaginas}
              </div>

              <button 
                className="btn btn-sm btn-outline-secondary"
                disabled={paginaSegura === totalPaginas}
                onClick={() => setPaginaActual(p => Math.min(totalPaginas, p + 1))}
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Confirmación y Notificación */}
      {modal.isOpen && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 1050 }} tabIndex={-1}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg" style={{ borderRadius: '1rem' }}>
              <div className="modal-header border-bottom-0 pt-4 px-4">
                <h5 className="modal-title fw-bold text-dark">{modal.title}</h5>
                <button type="button" className="btn-close" onClick={closeModal}></button>
              </div>
              <div className="modal-body px-4 pb-4">
                <p className="text-secondary fs-6 mb-0">{modal.message}</p>
              </div>
              <div className="modal-footer border-top-0 pb-4 px-4">
                <button type="button" className="btn btn-light fw-bold px-4" onClick={closeModal} style={{ borderRadius: '0.5rem' }}>
                  {modal.onConfirm ? 'Cancelar' : 'Entendido'}
                </button>
                {modal.onConfirm && (
                  <button type="button" className={`btn ${modal.confirmColor} fw-bold px-4`} onClick={modal.onConfirm} style={{ borderRadius: '0.5rem' }}>
                    Confirmar
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TablaUsuarios;
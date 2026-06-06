// src/pages/Usuarios/usuarios.tsx
import { ChevronLeft, ChevronRight, ChevronDown, ChevronUp, Search, UserCircle2 } from 'lucide-react';
import { useUsuarios } from './useUsuarios';

export default function Usuarios() {
  const {
    cargando, adminsIds,
    modal, closeModal,
    filtroTab, setFiltroTab,
    busqueda, setBusqueda,
    filtroNivel, setFiltroNivel,
    ordenColumna, ordenDireccion, toggleOrden,
    setPaginaActual, totalPaginas, paginaSegura, indexInicio, ITEMS_POR_PAGINA,
    usuariosProcesados, usuariosPaginados,
    handleHacerAdmin, handleQuitarAdmin, handleToggleBan
  } = useUsuarios();

  // Funciones puramente visuales
  const getAvatarColor = (name: string) => {
    const colors = ['bg-primary', 'bg-success', 'bg-danger', 'bg-warning', 'bg-info', 'bg-secondary', 'bg-dark'];
    const charCode = name ? name.charCodeAt(0) : 0;
    return colors[charCode % colors.length];
  };

  const getSortIcon = (columna: string) => {
    if (ordenColumna !== columna) return null;
    return ordenDireccion === 'asc' ? <ChevronUp size={16} className="ms-1" /> : <ChevronDown size={16} className="ms-1" />;
  };

  if (cargando) {
    return (
      <div className="container-fluid">
        <div className="d-flex flex-column justify-content-center align-items-center py-5" style={{ height: '80vh' }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
          <p className="mt-3 text-muted">Sincronizando base de datos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Cabecera del módulo */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="mb-0 fw-bold text-dark d-flex align-items-center">
            <UserCircle2 className="me-2 text-primary" size={28} />
            Gestión de Usuarios
          </h2>
          <p className="text-muted mb-0">Administra la comunidad, gestiona roles y aplica moderación.</p>
        </div>
      </div>

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
                    
                    {/* Columna 1: Avatar y Nombre */}
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
}
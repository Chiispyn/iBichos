import type { Usuario } from "../types/usuario";

import { useState } from 'react';
import { doc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';

interface TablaUsuariosProps {
  usuariosFiltrados: Usuario[];
  adminsIds: string[];
}

const TablaUsuarios = ({ usuariosFiltrados, adminsIds }: TablaUsuariosProps) => {

  const [modal, setModal] = useState<{isOpen: boolean, title: string, message: string, onConfirm: (() => void) | null, confirmColor: string}>({
    isOpen: false, title: '', message: '', onConfirm: null, confirmColor: 'btn-primary'
  });

  const showModal = (title: string, message: string, onConfirm: (() => void) | null, confirmColor: string = 'btn-primary') => {
    setModal({ isOpen: true, title, message, onConfirm, confirmColor });
  };

  const closeModal = () => setModal(prev => ({ ...prev, isOpen: false }));

  const handleHacerAdmin = (usuario: Usuario) => {
    showModal(
      'Nombrar Administrador',
      `¿Estás seguro que deseas promover a ${usuario.username} (${usuario.email}) como Administrador?`,
      async () => {
        closeModal();
        try {
          const adminRef = doc(db, 'admins', usuario.id);
          await setDoc(adminRef, { email: usuario.email, estado: 'activo', rol: 'admin' });
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
          showModal('¡Éxito!', `Se ha actualizado el estado de moderación de ${usuario.username}. (Recarga la página para ver los cambios)`, null);
        } catch (error) {
          console.error(`Error al ${accion}:`, error);
          showModal('Error', `Hubo un error al intentar ${accion}. Revisa la consola.`, null);
        }
      },
      color
    );
  };

  return (
    <div className="card shadow-sm border-0 mt-3">
      <div className="card-body p-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-dark">
              <tr>
                <th className="ps-4">ID</th>
                <th>Usuario</th>
                <th>Email</th>
                <th>Ubicación</th>
                <th className="text-center">Progreso</th>
                <th>Reputación</th>
                <th>F. Nacimiento</th>
                <th className="text-center pe-4">Acciones</th>
              </tr>
            </thead>
            
            <tbody>
              {usuariosFiltrados && usuariosFiltrados.length > 0 ? (
                usuariosFiltrados.map((usuario) => (
                  <tr key={usuario.id}>
                    <td className="ps-4">
                      <span className="fw-bold text-secondary">{usuario.id}</span>
                    </td>
                    
                    <td>
                      <div className="d-flex flex-column">
                        <span className="fw-bold">
                          {usuario.username}
                          {adminsIds.includes(usuario.id) && (
                            <span className="badge bg-danger ms-2" style={{ fontSize: '0.6rem' }}>Moderador</span>
                          )}
                        </span>
                        <small className="text-muted text-uppercase" style={{ fontSize: '0.7rem' }}>
                          Género: {usuario.genre}
                        </small>
                      </div>
                    </td>

                    <td>{usuario.email}</td>

                    <td>
                      <div className="d-flex flex-column text-truncate" style={{ maxWidth: '200px' }}>
                        <span className="badge bg-info text-dark align-self-start mb-1" style={{ fontSize: '0.65rem' }}>
                          {usuario.region}
                        </span>
                        <small className="text-muted">{usuario.comuna}</small>
                      </div>
                    </td>

                    <td className="text-center">
                      <span className="badge bg-success me-1">Nvl {usuario.level}</span>
                      <small className="text-muted d-block">{usuario.xp} XP</small>
                    </td>

                    <td>
                      <div className="d-flex flex-column text-truncate">
                        <span className={`badge ${usuario.isShadowBanned ? 'bg-danger' : 'bg-success'} align-self-start mb-1`} style={{ fontSize: '0.65rem' }}>
                          {usuario.isShadowBanned ? 'BANEADO' : 'CUENTA ACTIVA'}
                        </span>
                        <small className="text-muted">{usuario.strikes || 0} advertencias (strikes)</small>
                      </div>
                    </td>

                    <td>
                      <small>{usuario.birthdate}</small>
                    </td>

                    <td className="text-center pe-4">
                      <div className="d-flex justify-content-center gap-2">
                        <button 
                          className={`btn btn-sm ${adminsIds.includes(usuario.id) ? 'btn-outline-danger' : 'btn-outline-primary'}`}
                          title={adminsIds.includes(usuario.id) ? "Quitar Administrador" : "Nombrar Administrador"}
                          onClick={() => adminsIds.includes(usuario.id) ? handleQuitarAdmin(usuario) : handleHacerAdmin(usuario)}
                        >
                          <i className={`bi ${adminsIds.includes(usuario.id) ? 'bi-shield-minus' : 'bi-shield-check'} me-1`}></i> 
                          {adminsIds.includes(usuario.id) ? 'Revocar Admin' : 'Hacer Admin'}
                        </button>
                        
                        <button 
                          className={`btn btn-sm ${usuario.isShadowBanned ? 'btn-success' : 'btn-dark'}`}
                          title={usuario.isShadowBanned ? "Levantar Shadowban" : "Aplicar Shadowban"}
                          onClick={() => handleToggleBan(usuario)}
                        >
                          <i className={`bi ${usuario.isShadowBanned ? 'bi-unlock' : 'bi-slash-circle'} me-1`}></i> 
                          {usuario.isShadowBanned ? 'Desbanear' : 'Banear'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="text-center py-5 text-muted">
                    <p className="mb-0">No se encontraron usuarios registrados.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal de Confirmación y Notificación */}
      {modal.isOpen && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.6)' }} tabIndex={-1}>
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
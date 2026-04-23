import type { Usuario } from "../types/usuario";

import { doc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';

interface TablaUsuariosProps {
  usuariosFiltrados: Usuario[];
  adminsIds: string[];
}

const TablaUsuarios = ({ usuariosFiltrados, adminsIds }: TablaUsuariosProps) => {

  const handleHacerAdmin = async (usuario: Usuario) => {
    if (window.confirm(`¿Estás seguro que deseas promover a ${usuario.username} (${usuario.email}) como Administrador?`)) {
      try {
        const adminRef = doc(db, 'admins', usuario.id);
        await setDoc(adminRef, {
          email: usuario.email,
          estado: 'activo',
          rol: 'admin'
        });
        alert(`¡Éxito! ${usuario.username} ahora es administrador del Dashboard. (Recarga la página para ver los cambios)`);
      } catch (error) {
        console.error("Error al promover a admin:", error);
        alert("Hubo un error al intentar hacerlo administrador. Revisa la consola.");
      }
    }
  };

  const handleQuitarAdmin = async (usuario: Usuario) => {
    if (window.confirm(`⚠️ ¿Estás seguro que deseas QUITAR el acceso de Administrador a ${usuario.username}?`)) {
      try {
        const adminRef = doc(db, 'admins', usuario.id);
        await deleteDoc(adminRef);
        alert(`Se ha revocado el acceso a ${usuario.username}. (Recarga la página para ver los cambios)`);
      } catch (error) {
        console.error("Error al revocar admin:", error);
        alert("Hubo un error al intentar quitarle el acceso. Revisa la consola.");
      }
    }
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
                            <span className="badge bg-danger ms-2" style={{ fontSize: '0.6rem' }}>STAFF</span>
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
                      <small>{usuario.birthdate}</small>
                    </td>

                    <td className="text-center pe-4">
                      <div className="btn-group btn-group-sm">
                        {adminsIds.includes(usuario.id) ? (
                          <button 
                            className="btn btn-outline-danger" 
                            title="Quitar Admin"
                            onClick={() => handleQuitarAdmin(usuario)}
                          >
                            Revocar Admin
                          </button>
                        ) : (
                          <button 
                            className="btn btn-outline-success" 
                            title="Hacer Admin"
                            onClick={() => handleHacerAdmin(usuario)}
                          >
                            Hacer Admin
                          </button>
                        )}
                        <button className="btn btn-outline-secondary" title="Banear">
                          <i className="bi bi-slash-circle"></i>
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
    </div>
  );
};

export default TablaUsuarios;
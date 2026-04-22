import type { Usuario } from "../types/usuario";

interface TablaUsuariosProps {
  usuariosFiltrados: Usuario[];
}

const TablaUsuarios = ({ usuariosFiltrados }: TablaUsuariosProps) => {
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
                        <span className="fw-bold">{usuario.username}</span>
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
                        <button className="btn btn-outline-primary" title="Ver">
                          <i className="bi bi-pencil"></i> Ver
                        </button>
                        <button className="btn btn-outline-danger" title="Eliminar">
                          <i className="bi bi-trash"></i>
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
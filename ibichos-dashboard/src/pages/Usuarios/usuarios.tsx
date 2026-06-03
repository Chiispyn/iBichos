// src/pages/Usuarios/Usuarios.tsx
import { useUsuarios } from './useUsuarios';
import TablaUsuarios from './tablaUsuarios';

export default function Usuarios() {
  // Extraemos la información de nuestro hook personalizado
  const { listaUsuarios, adminsIds, cargando } = useUsuarios();

  return (
    <div className="container-fluid">
      {/* Cabecera del módulo */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>Gestión de Usuarios</h2>
          <p className="text-muted mb-0">Conectado a la colección "users" de Firestore</p>
        </div>
      </div>

      {/* Control de la vista: Spinner de carga vs Tabla */}
      {cargando ? (
        <div className="d-flex flex-column justify-content-center align-items-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
          <p className="mt-3 text-muted">Sincronizando base de datos...</p>
        </div>
      ) : (
        <TablaUsuarios usuariosFiltrados={listaUsuarios} adminsIds={adminsIds} />
      )}
    </div>
  );
}
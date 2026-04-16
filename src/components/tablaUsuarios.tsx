import type { Usuario } from "../types/usuario";

// 1. Definimos la interface para las props del componente
interface TablaUsuariosProps {
  usuariosFiltrados: Usuario[];
}

// 2. Pasamos las props al componente (desestructuradas)
const TablaUsuarios = ({ usuariosFiltrados }: TablaUsuariosProps) => {
  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full">
          <thead className="bg-gray-800 text-white">
            <tr>
              <th className="py-3 px-4 text-left">RUT</th>
              <th className="py-3 px-4 text-left">Nombre</th>
              <th className="py-3 px-4 text-left">Nombre de usuario</th>
              <th className="py-3 px-4 text-left">Email</th>
              <th className="py-3 px-4 text-left">Ubicación</th>
              <th className="py-3 px-4 text-center">F. Nacimiento</th>
              <th className="py-3 px-4 text-center">Acciones</th>
            </tr>
          </thead>
          
          <tbody className="divide-y divide-gray-200">
            {/* 3. Agregamos validación opcional por si la lista viene undefined */}
            {usuariosFiltrados && usuariosFiltrados.length > 0 ? (
              usuariosFiltrados.map((usuario) => (
                <tr key={usuario.rut} className="hover:bg-gray-50 transition-colors">
                  <td className="py-3 px-4 font-mono text-sm text-gray-600">
                    {usuario.rut}
                  </td>

                  <td className="py-3 px-4">
                    <p className="font-bold text-gray-800">{usuario.name}</p>
                  </td>

                  <td className="py-3 px-4 text-gray-600 text-sm">
                    {usuario.email}
                  </td>

                  <td className="py-3 px-4">
                    <div className="flex flex-col">
                      <span className="text-xs font-bold text-blue-700 uppercase">
                        {usuario.region}
                      </span>
                      <span className="text-sm text-gray-500">
                        {usuario.comuna}
                      </span>
                    </div>
                  </td>

                  <td className="py-3 px-4 text-center text-sm text-gray-600">
                    {usuario.birthdate}
                  </td>

                  <td className="py-3 px-4 text-center space-x-2">
                    <button className="text-blue-600 hover:text-blue-900 font-medium text-sm hover:underline">
                      Editar
                    </button>
                    <button className="text-red-600 hover:text-red-900 font-medium text-sm hover:underline">
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="py-8 text-center text-gray-500">
                  No se encontraron usuarios que coincidan con tu búsqueda.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TablaUsuarios; // 4. No olvides exportar el componente
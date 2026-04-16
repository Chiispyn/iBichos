import TablaUsuarios from '../components/tablaUsuarios'
import type { Usuario } from '../types/usuario';


function Usuarios() {
  const usuariosFiltrados: Usuario[] = [];
  return (
    
    <>
        <h1>Modulo de Usuarios</h1>
        <TablaUsuarios usuariosFiltrados={usuariosFiltrados}></TablaUsuarios>
      
    </>
  )
}

export default Usuarios

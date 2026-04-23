import { useState, useEffect } from 'react';
import { collection, getDocs } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import type { Usuario } from '../types/usuario'; // 🟢 Importamos tu excelente interfaz
import TablaUsuarios from '../components/tablaUsuarios';

export default function Usuarios() {
  const [listaUsuarios, setListaUsuarios] = useState<Usuario[]>([]);
  const [adminsIds, setAdminsIds] = useState<string[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const obtenerUsuariosDeFirebase = async () => {
      try {
        // Obtenemos los usuarios y los admins en paralelo
        const [usersSnap, adminsSnap] = await Promise.all([
          getDocs(collection(db, "users")),
          getDocs(collection(db, "admins"))
        ]);
        
        const adminIdsList = adminsSnap.docs.map(doc => doc.id);
        setAdminsIds(adminIdsList);
        
        // Diccionarios de traducción
        const traducirGenero = (g: string) => {
          const dict: Record<string, string> = { MALE: 'Masculino', FEMALE: 'Femenino', OTHER: 'Otro', PREFER_NOT_TO_SAY: 'Reservado', UNSPECIFIED: 'No definido' };
          return dict[g] || g;
        };

        const traducirNivel = (lvl: string) => {
          const dict: Record<string, string> = { CASUAL: 'Casual', AMATEUR: 'Amateur', EXPLORER: 'Explorador', ENTOMOLOGIST: 'Entomólogo', BUG_MASTER: 'Maestro de Bichos' };
          return dict[lvl] || lvl;
        };

        // 2. Mapeo exacto para cumplir con tu interfaz 'Usuario'
        const usuariosMapeados: Usuario[] = usersSnap.docs.map(doc => {
          const data = doc.data();
          
          return {
            id: doc.id,
            username: data.displayName || 'Sin nombre',
            genre: traducirGenero(data.gender || 'UNSPECIFIED'),
            email: data.email || '',
            birthdate: data.birthDate || 'N/A', // En Android es birthDate (con D mayúscula)
            region: data.region || 'Sin región',
            comuna: data.city || 'Sin comuna', // ¡En Android se guardaba como city!
            level: traducirNivel(data.gamification?.level || 'CASUAL'), // Traducido al español
            xp: data.xp || 0,
            createdAt: data.createdAt ? new Date(data.createdAt.toMillis()).toLocaleDateString() : undefined
          };
        });

        setListaUsuarios(usuariosMapeados);
      } catch (error) {
        console.error("Error al obtener la lista de usuarios: ", error);
      } finally {
        setCargando(false);
      }
    };

    obtenerUsuariosDeFirebase();
  }, []);

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
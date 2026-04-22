import { useState, useEffect } from 'react';
import { collection, getDocs } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import type { Usuario } from '../types/usuario'; // 🟢 Importamos tu excelente interfaz
import TablaUsuarios from '../components/tablaUsuarios';

export default function Usuarios() {
  // 1. Estados estrictamente tipados
  const [listaUsuarios, setListaUsuarios] = useState<Usuario[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const obtenerUsuariosDeFirebase = async () => {
      try {
        const querySnapshot = await getDocs(collection(db, "users"));
        
        // 2. Mapeo exacto para cumplir con tu interfaz 'Usuario'
        const usuariosMapeados: Usuario[] = querySnapshot.docs.map(doc => {
          const data = doc.data();
          
          return {
            id: doc.id, // Fundamental para React (key) y futuras ediciones
            username: data.displayName || 'Sin nombre',
            genre: data.gender || 'NO DEFINIDO',
            email: data.email || '',
            birthdate: data.birthDate || 'N/A',
            region: data.region || 'Sin región',
            comuna: data.comuna || 'Sin comuna',
            level: data.level || 'Casual',
            xp: data.xp || 0,
            // Si la base de datos no tiene createdAt, lo dejamos como undefined
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
        <TablaUsuarios usuariosFiltrados={listaUsuarios} />
      )}
    </div>
  );
}
import { useState, useEffect } from 'react';
import { collection, getDocs } from 'firebase/firestore';
import { db } from "../config/firebaseConfig";
import TablaUsuarios from '../components/tablaUsuarios';
import type { Usuario } from '../types/usuario';

// Import opcional por si quieres volver a los mocks después
// import { MOCK_USUARIOS } from '../mocks/usersdates'; 

function Usuarios() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const obtenerUsuarios = async () => {
      try {
        // 1. Referencia a tu colección "users" (tal cual sale en tu foto)
        const usersCol = collection(db, 'users');
        const snapshot = await getDocs(usersCol);
        
        // 2. Mapeamos los datos de Firestore a tu interfaz Usuario
        const listaDocs = snapshot.docs.map(doc => {
          const data = doc.data();
          return {
            // Usamos el ID de Firestore como RUT si el campo no existe aún
            rut: data.rut || doc.id, 
            username: data.displayName || "Sin nombre", // Firestore usa displayName
            email: data.email || "",
            level: data.level || "Novato",
            xp: data.xp || 0,
            // Campos que están en tu interfaz pero no en tu captura (les damos valores por defecto)
            genre: data.genre || "No definido",
            birthdate: data.birthdate || "N/A",
            region: data.region || "Sin región",
            comuna: data.comuna || "Sin comuna",
          } as Usuario;
        });

        setUsuarios(listaDocs);
      } catch (error) {
        console.error("Error al traer usuarios de Firestore:", error);
      } finally {
        setCargando(false);
      }
    };

    obtenerUsuarios();
  }, []);

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="fw-bold">Gestión de Usuarios</h1>
          <p className="text-muted">Conectado a la colección "users" de Firestore</p>
        </div>
        <button className="btn btn-primary shadow-sm">
          <i className="bi bi-plus-lg me-2"></i>Nuevo Usuario
        </button>
      </div>

      {/* 3. Manejo de estado: Cargando vs Tabla */}
      {cargando ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="mt-2 text-muted">Buscando cazadores en la base de datos...</p>
        </div>
      ) : (
        <TablaUsuarios usuariosFiltrados={usuarios} />
      )}
    </div>
  );
}

export default Usuarios;
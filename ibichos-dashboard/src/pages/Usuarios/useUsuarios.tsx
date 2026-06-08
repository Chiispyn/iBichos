// src/pages/Usuarios/useUsuarios.ts
import { useState, useEffect, useMemo } from 'react';
import { collection, onSnapshot, doc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../../config/firebaseConfig';
import { useAuth } from '../../context/authcontext';
import type { Usuario } from '../../types/usuario';

// --- FUNCIONES DE FORMATO ---
const traducirGenero = (g: string) => {
  const dict: Record<string, string> = { MALE: 'Masculino', FEMALE: 'Femenino', OTHER: 'Otro', PREFER_NOT_TO_SAY: 'Reservado', UNSPECIFIED: 'No definido' };
  return dict[g] || g;
};

const traducirNivel = (lvl: string) => {
  const dict: Record<string, string> = { CASUAL: 'Casual', AMATEUR: 'Amateur', EXPLORER: 'Explorador', ENTOMOLOGIST: 'Entomólogo', BUG_MASTER: 'Maestro de Bichos' };
  return dict[lvl] || lvl;
};

export function useUsuarios() {
  const { user } = useAuth();
  
  // Estados Base de Datos
  const [listaUsuarios, setListaUsuarios] = useState<Usuario[]>([]);
  const [adminsIds, setAdminsIds] = useState<string[]>([]);
  const [cargando, setCargando] = useState(true);

  // Estados de la Interfaz (Tabla)
  const [modal, setModal] = useState<{isOpen: boolean, title: string, message: string, onConfirm: (() => void) | null, confirmColor: string}>({
    isOpen: false, title: '', message: '', onConfirm: null, confirmColor: 'btn-primary'
  });
  const [filtroTab, setFiltroTab] = useState<'Todos' | 'Jugadores' | 'Moderadores' | 'Baneados'>('Todos');
  const [ordenColumna, setOrdenColumna] = useState<'username' | 'xp' | 'level'>('xp');
  const [ordenDireccion, setOrdenDireccion] = useState<'asc' | 'desc'>('desc');
  const [paginaActual, setPaginaActual] = useState(1);
  const [busqueda, setBusqueda] = useState('');
  const [filtroNivel, setFiltroNivel] = useState('Todos');
  const ITEMS_POR_PAGINA = 10;

  // 1. OBTENCIÓN DE DATOS (Con mapeo seguro para evitar error de Timestamp)
  useEffect(() => {
    setCargando(true);
    
    const unsubscribeAdmins = onSnapshot(collection(db, "admins"), (snap) => {
      setAdminsIds(snap.docs.map(doc => doc.id));
    });

    const unsubscribeUsers = onSnapshot(collection(db, "users"), (snap) => {
      const usuariosMapeados: Usuario[] = snap.docs.map(doc => {
        const data = doc.data();
        return {
          id: doc.id,
          username: data.displayName || 'Sin nombre',
          genre: traducirGenero(data.gender || 'UNSPECIFIED'),
          email: data.email || '',
          birthdate: data.birthDate || 'N/A',
          region: data.region || 'Sin región',
          comuna: data.city || 'Sin comuna',
          level: traducirNivel(data.gamification?.level || 'CASUAL'),
          xp: data.xp || 0,
          createdAt: data.createdAt ? new Date(data.createdAt.toMillis()).toLocaleDateString('es-CL') : 'N/A',
          isShadowBanned: data.isShadowBanned || false,
          strikes: data.strikes || 0
        };
      });
      setListaUsuarios(usuariosMapeados);
      setCargando(false);
    }, (error) => {
      console.error("Error en tiempo real (Usuarios):", error);
      setCargando(false);
    });

    return () => {
      unsubscribeAdmins();
      unsubscribeUsers();
    };
  }, []);

  // 2. UTILIDADES DE MODAL Y AUDITORÍA
  const showModal = (title: string, message: string, onConfirm: (() => void) | null, confirmColor: string = 'btn-primary') => {
    setModal({ isOpen: true, title, message, onConfirm, confirmColor });
  };
  const closeModal = () => setModal(prev => ({ ...prev, isOpen: false }));

  const logAudit = async (action: string, targetId: string) => {
    try {
      const logRef = doc(collection(db, 'moderation_logs'));
      await setDoc(logRef, {
        adminId: user?.uid || 'unknown',
        adminEmail: user?.email || 'Admin',
        action, targetId, targetType: 'USER', timestamp: new Date()
      });
    } catch (e) {
      console.warn("Error guardando audit log:", e);
    }
  };

  // 3. ACCIONES DE MODERACIÓN
  const handleHacerAdmin = (usuario: Usuario) => {
    showModal(
      'Nombrar Administrador',
      `¿Estás seguro que deseas promover a ${usuario.username} (${usuario.email}) como Administrador?`,
      async () => {
        closeModal();
        try {
          const adminRef = doc(db, 'admins', usuario.id);
          await setDoc(adminRef, { email: usuario.email, status: 'active', role: 'admin' });
          await logAudit('MAKE_ADMIN', usuario.id);
          showModal('¡Éxito!', `${usuario.username} ahora es administrador.`, null);
        } catch (error) {
          console.error("Error al promover a admin:", error);
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
          await logAudit('REMOVE_ADMIN', usuario.id);
          showModal('¡Revocado!', `Se ha revocado el acceso a ${usuario.username}.`, null);
        } catch (error) {
          console.error("Error al revocar admin:", error);
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
          await logAudit(isCurrentlyBanned ? 'UNBAN_USER' : 'BAN_USER', usuario.id);
          showModal('¡Éxito!', `Se ha actualizado el estado de moderación de ${usuario.username}.`, null);
        } catch (error) {
          console.error(`Error al ${accion}:`, error);
        }
      },
      color
    );
  };

  // 4. LÓGICA DE TABLA (Filtros y Orden)
  const usuariosProcesados = useMemo(() => {
    let result = [...listaUsuarios];

    if (filtroTab === 'Moderadores') result = result.filter(u => adminsIds.includes(u.id));
    else if (filtroTab === 'Baneados') result = result.filter(u => u.isShadowBanned);
    else if (filtroTab === 'Jugadores') result = result.filter(u => !adminsIds.includes(u.id));

    if (filtroNivel !== 'Todos') {
      result = result.filter(u => u.level && u.level.toUpperCase() === filtroNivel.toUpperCase());
    }

    if (busqueda.trim() !== '') {
      const b = busqueda.toLowerCase();
      result = result.filter(u => 
        (u.username && u.username.toLowerCase().includes(b)) || 
        (u.email && u.email.toLowerCase().includes(b))
      );
    }

    result.sort((a, b) => {
      let valA = a[ordenColumna] || '';
      let valB = b[ordenColumna] || '';
      
      if (typeof valA === 'string' && typeof valB === 'string') {
        valA = valA.toLowerCase();
        valB = valB.toLowerCase();
      }

      if (valA < valB) return ordenDireccion === 'asc' ? -1 : 1;
      if (valA > valB) return ordenDireccion === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  }, [listaUsuarios, adminsIds, filtroTab, filtroNivel, busqueda, ordenColumna, ordenDireccion]);

  const totalPaginas = Math.max(1, Math.ceil(usuariosProcesados.length / ITEMS_POR_PAGINA));
  const paginaSegura = Math.min(paginaActual, totalPaginas);
  const indexInicio = (paginaSegura - 1) * ITEMS_POR_PAGINA;
  const usuariosPaginados = usuariosProcesados.slice(indexInicio, indexInicio + ITEMS_POR_PAGINA);

  const toggleOrden = (columna: 'username' | 'xp' | 'level') => {
    if (ordenColumna === columna) {
      setOrdenDireccion(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setOrdenColumna(columna);
      setOrdenDireccion('desc');
    }
  };

  return {
    cargando, adminsIds,
    modal, closeModal,
    filtroTab, setFiltroTab,
    busqueda, setBusqueda,
    filtroNivel, setFiltroNivel,
    ordenColumna, ordenDireccion, toggleOrden,
    paginaActual, setPaginaActual,
    totalPaginas, paginaSegura, indexInicio, ITEMS_POR_PAGINA,
    usuariosProcesados, usuariosPaginados,
    handleHacerAdmin, handleQuitarAdmin, handleToggleBan
  };
}
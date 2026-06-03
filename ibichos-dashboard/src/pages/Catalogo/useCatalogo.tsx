// src/pages/Catalogo/useCatalogo.ts
import { useEffect, useState } from 'react';
import { collection, onSnapshot, doc, updateDoc, setDoc } from 'firebase/firestore';
import { db } from '../../config/firebaseConfig';
import { useAuth } from '../../context/authcontext';
import type { Captura } from '../../types/captura';

// --- FUNCIONES DE FORMATO (Integradas en el Hook) ---
const traducirPeligro = (level: string) => {
  switch(level) {
    case 'HARMLESS': return 'Inofensivo';
    case 'VENOMOUS': return 'Venenoso';
    case 'CAUTION': return 'Precaución';
    default: return 'Desconocido';
  }
};

const getDangerBadge = (level: string) => {
  switch(level) {
    case 'HARMLESS': return 'bg-success';
    case 'VENOMOUS': return 'bg-danger';
    case 'CAUTION': return 'bg-warning text-dark';
    default: return 'bg-secondary';
  }
};

export function useCatalogo() {
  const { user } = useAuth();
  const [capturas, setCapturas] = useState<Captura[]>([]);
  const [cargando, setCargando] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [selectedImg, setSelectedImg] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [targetId, setTargetId] = useState<string | null>(null);
  const [userMap, setUserMap] = useState<Record<string, { name: string; email: string }>>({});

  useEffect(() => {
    import('firebase/firestore').then(({ getDocs, collection: col }) => {
      getDocs(col(db, 'users')).then(snap => {
        const map: Record<string, { name: string; email: string }> = {};
        snap.forEach(d => {
          map[d.id] = { name: d.data().displayName || 'Sin nombre', email: d.data().email || '' };
        });
        setUserMap(map);
      });
    });
  }, []);

  useEffect(() => {
    setCargando(true);
    const unsubscribe = onSnapshot(collection(db, "captures"), (querySnapshot) => {
      const datos: Captura[] = querySnapshot.docs
        .map(doc => {
          const d = doc.data();
          const currentStatus = d.status || d.validationStatus;
          const isApproved = currentStatus === 'APPROVED' || 
                             (!currentStatus && !d.needsReview && (d.probability || 0) >= 0.75);
          
          return { id: doc.id, ...d, isApproved, status: currentStatus } as any;
        })
        .filter(cap => cap.isApproved && cap.status !== 'DELETED')
        .map(cap => ({
          id: cap.id,
          imageUrl: cap.imageUrl || '',
          category: cap.category || 'OTHER',
          dangerLevel: cap.dangerLevel || 'UNKNOWN',
          confidence: cap.probability || 0,
          userId: cap.userId || 'Anónimo',
          insectName: cap.insectName,
          scientificName: cap.scientificName,
          status: cap.status || 'APPROVED',
          timestamp: cap.timestamp
        }));

      datos.sort((a, b) => {
        const timeA = a.timestamp?.toDate ? a.timestamp.toDate().getTime() : new Date(a.timestamp || 0).getTime();
        const timeB = b.timestamp?.toDate ? b.timestamp.toDate().getTime() : new Date(b.timestamp || 0).getTime();
        return timeB - timeA;
      });

      setCapturas(datos);
      setCargando(false);
    }, (error) => {
      console.error("Error en tiempo real (Catálogo):", error);
      setCargando(false);
    });

    return () => unsubscribe();
  }, []);

  const handleUpdate = async (id: string, updates: any) => {
    try {
      const docRef = doc(db, 'captures', id);
      await updateDoc(docRef, {
        ...updates,
        moderatedBy: user?.uid || 'ADMIN',
        moderatorEmail: user?.email || 'Admin',
        moderatedAt: new Date()
      });

      try {
        const actionType = updates.category ? 'UPDATE_CATEGORY' : (updates.dangerLevel ? 'UPDATE_DANGER_LEVEL' : 'UPDATE_CAPTURE');
        const logRef = doc(collection(db, 'moderation_logs'));
        await setDoc(logRef, {
          adminId: user?.uid || 'unknown',
          adminEmail: user?.email || 'Admin',
          action: actionType,
          targetId: id,
          targetType: 'CAPTURE',
          timestamp: new Date()
        });
      } catch (e) {
        console.warn("No se pudo guardar el log en Catálogo:", e);
      }

      setCapturas(prev => prev.map(cap => cap.id === id ? { ...cap, ...updates } : cap));
    } catch (error) {
      console.error("Error al actualizar especie:", error);
    }
  };

  const handleReject = async () => {
    if (!targetId) return;
    try {
      const docRef = doc(db, 'captures', targetId);
      await updateDoc(docRef, { 
        status: 'REJECTED',
        needsReview: false,
        moderatedBy: user?.uid || 'ADMIN',
        moderatorEmail: user?.email || 'Admin',
        moderatedAt: new Date()
      });

      try {
        const logRef = doc(collection(db, 'moderation_logs'));
        await setDoc(logRef, {
          adminId: user?.uid || 'unknown',
          adminEmail: user?.email || 'Admin',
          action: 'REJECT_CAPTURE',
          targetId: targetId,
          targetType: 'CAPTURE',
          timestamp: new Date()
        });
      } catch (e) {
        console.warn("No se pudo guardar el log en Catálogo:", e);
      }

      setCapturas(prev => prev.filter(cap => cap.id !== targetId));
      setShowModal(false);
      setTargetId(null);
    } catch (error) {
      console.error("Error al rechazar especie:", error);
    }
  };

  const openDeleteModal = (id: string) => {
    setTargetId(id);
    setShowModal(true);
  };

  const filtered = capturas.filter(cap => {
    const query = busqueda.toLowerCase();
    const insectName = (cap.insectName || '').toLowerCase();
    const scientificName = (cap.scientificName || '').toLowerCase();
    const userInfo = userMap[cap.userId];
    const userName = (userInfo?.name || '').toLowerCase();
    const userEmail = (userInfo?.email || '').toLowerCase();

    return insectName.includes(query) ||
           scientificName.includes(query) ||
           userName.includes(query) ||
           userEmail.includes(query);
  });

  return {
    cargando,
    busqueda, setBusqueda,
    selectedImg, setSelectedImg,
    showModal, setShowModal,
    userMap,
    filtered,
    handleUpdate,
    handleReject,
    openDeleteModal,
    getDangerBadge,
    traducirPeligro
  };
}
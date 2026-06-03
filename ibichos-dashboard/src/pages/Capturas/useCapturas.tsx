// src/pages/Capturas/useCapturas.ts
import { useEffect, useState } from 'react';
import { collection, onSnapshot, doc, updateDoc, increment, setDoc } from 'firebase/firestore';
import { db } from '../../config/firebaseConfig';
import { useAuth } from '../../context/authcontext';
import type { Captura } from '../../types/captura';

// --- FUNCIONES DE FORMATO (Integradas en el Hook) ---
const getDangerColor = (level: string) => {
  switch (level) {
    case 'HARMLESS': return 'success';
    case 'VENOMOUS': return 'danger';
    case 'CAUTION': return 'warning';
    default: return 'secondary';
  }
};

const traducirPeligro = (level: string) => {
  switch (level) {
    case 'HARMLESS': return 'Inofensivo';
    case 'VENOMOUS': return 'Venenoso';
    case 'CAUTION': return 'Precaución';
    default: return 'Desconocido';
  }
};

export function useCapturas() {
  const { user } = useAuth();
  const [capturas, setCapturas] = useState<Captura[]>([]);
  const [filtro, setFiltro] = useState<'PENDING_REVIEW' | 'REJECTED'>('PENDING_REVIEW');
  const [cargando, setCargando] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [selectedImg, setSelectedImg] = useState<string | null>(null);
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
          return {
            id: doc.id,
            imageUrl: d.imageUrl || '',
            category: d.category || 'OTHER',
            dangerLevel: d.dangerLevel || 'UNKNOWN',
            confidence: d.probability || 0,
            needsReview: d.needsReview || false,
            status: d.status || d.validationStatus || (d.needsReview ? 'PENDING_REVIEW' : (d.probability < 0.40 ? 'REJECTED' : 'APPROVED')),
            userId: d.userId || 'Anónimo',
            insectName: d.insectName,
            scientificName: d.scientificName,
            lat: d.latitude,
            lng: d.longitude,
            moderatedBy: d.moderatedBy,
            moderatorEmail: d.moderatorEmail,
            timestamp: d.timestamp
          };
        })
        .filter(cap => cap.status !== 'DELETED');

      datos.sort((a, b) => {
        if (a.status === 'PENDING_REVIEW' && b.status !== 'PENDING_REVIEW') return -1;
        if (a.status !== 'PENDING_REVIEW' && b.status === 'PENDING_REVIEW') return 1;

        const timeA = a.timestamp?.toDate ? a.timestamp.toDate().getTime() : new Date(a.timestamp || 0).getTime();
        const timeB = b.timestamp?.toDate ? b.timestamp.toDate().getTime() : new Date(b.timestamp || 0).getTime();

        return timeB - timeA;
      });

      setCapturas(datos);
      setCargando(false);
    }, (error) => {
      console.error("Error en tiempo real (Capturas):", error);
      setCargando(false);
    });

    return () => unsubscribe();
  }, []);

  const handleModeracion = async (id: string, nuevoEstado: 'APPROVED' | 'REJECTED', nuevaCat?: string, penalizar: boolean = false) => {
    try {
      const capturaRef = doc(db, 'captures', id);
      const updates: any = {
        status: nuevoEstado,
        needsReview: false,
        moderatedBy: user?.uid || 'ADMIN',
        moderatorEmail: user?.email || 'Admin',
        moderatedAt: new Date()
      };

      if (nuevaCat) updates.category = nuevaCat;

      await updateDoc(capturaRef, updates);

      try {
        const logRef = doc(collection(db, 'moderation_logs'));
        await setDoc(logRef, {
          adminId: user?.uid || 'unknown',
          adminEmail: user?.email || 'Admin',
          action: penalizar ? 'STRIKE_AND_REJECT' : (nuevoEstado === 'APPROVED' ? 'APPROVE_CAPTURE' : 'REJECT_CAPTURE'),
          targetId: id,
          targetType: 'CAPTURE',
          timestamp: new Date()
        });
      } catch (e) {
        console.warn("No se pudo guardar el log (¿Faltan reglas en Firestore?):", e);
      }

      if (penalizar) {
        const cap = capturas.find(c => c.id === id);
        if (cap && cap.userId && cap.userId !== 'Anónimo') {
          const userRef = doc(db, 'users', cap.userId);
          await updateDoc(userRef, { strikes: increment(1) });
          alert("¡Strike aplicado! El usuario ha sido penalizado por fraude.");
        }
      }

      setCapturas(prev => prev.map(cap =>
        cap.id === id ? { ...cap, status: nuevoEstado, needsReview: false, category: nuevaCat || cap.category, moderatedBy: updates.moderatedBy, moderatorEmail: updates.moderatorEmail } : cap
      ));
    } catch (error) {
      console.error("Error al moderar la captura:", error);
    }
  };

  const handleEliminar = async () => {
    if (!selectedId) return;
    try {
      const capturaRef = doc(db, 'captures', selectedId);
      await updateDoc(capturaRef, {
        status: 'DELETED',
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
          action: 'HIDE_CAPTURE',
          targetId: selectedId,
          targetType: 'CAPTURE',
          timestamp: new Date()
        });
      } catch (e) {
        console.warn("No se pudo guardar el log:", e);
      }

      setCapturas(prev => prev.filter(cap => cap.id !== selectedId));
      setShowModal(false);
      setSelectedId(null);
      alert("Captura ocultada. Se mantiene en la base de datos para estadísticas.");
    } catch (error) {
      console.error("Error al ocultar captura:", error);
    }
  };

  const openModal = (id: string) => {
    setSelectedId(id);
    setShowModal(true);
  };

  const capturasFiltradas = capturas.filter(c => c.status === filtro);

  return {
    capturas,
    capturasFiltradas,
    filtro, setFiltro,
    cargando,
    showModal, setShowModal,
    selectedId, setSelectedId,
    selectedImg, setSelectedImg,
    userMap,
    handleModeracion,
    handleEliminar,
    openModal,
    getDangerColor,
    traducirPeligro
  };
}
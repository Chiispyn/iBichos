// src/pages/Auditoria/useAuditoria.ts
import { useEffect, useState } from 'react';
import { collection, query, orderBy, onSnapshot } from 'firebase/firestore';
import { db } from '../../config/firebaseConfig';
import type { AuditLog } from '../../types/auditlog';

export function useAuditoria() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setCargando(true);
    const q = query(collection(db, 'moderation_logs'), orderBy('timestamp', 'desc'));
    
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map(doc => {
        const docData = doc.data();
        return {
          id: doc.id,
          adminId: docData.adminId,
          adminEmail: docData.adminEmail,
          action: docData.action,
          targetId: docData.targetId,
          targetType: docData.targetType,
          timestamp: docData.timestamp?.toDate ? docData.timestamp.toDate() : new Date(docData.timestamp)
        } as AuditLog;
      });
      setLogs(data);
      setCargando(false);
    }, (error) => {
      console.error("Error cargando auditoría:", error);
      setCargando(false);
    });

    return () => unsubscribe();
  }, []);

  return {
    logs,
    cargando
  };
}
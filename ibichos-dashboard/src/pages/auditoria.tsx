import { useEffect, useState } from 'react';
import { collection, query, orderBy, onSnapshot } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import { ShieldAlert, User, Image, Clock, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

interface AuditLog {
  id: string;
  adminId: string;
  adminEmail?: string;
  action: string;
  targetId: string;
  targetType: string;
  timestamp: Date;
}

export default function Auditoria() {
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

  const getActionIcon = (action: string) => {
    if (action.includes('REJECT') || action.includes('BAN') || action.includes('HIDE') || action.includes('REMOVE')) return <XCircle className="text-danger" size={18} />;
    if (action.includes('APPROVE') || action.includes('MAKE_ADMIN') || action.includes('UNBAN')) return <CheckCircle2 className="text-success" size={18} />;
    if (action.includes('STRIKE')) return <AlertTriangle className="text-warning" size={18} />;
    return <Clock className="text-secondary" size={18} />;
  };

  const getActionLabel = (action: string) => {
    const dict: Record<string, string> = {
      'REJECT_CAPTURE': 'Rechazó una captura',
      'APPROVE_CAPTURE': 'Aprobó una captura dudosa',
      'HIDE_CAPTURE': 'Ocultó/Eliminó una captura',
      'STRIKE_AND_REJECT': 'Rechazó por fraude y dio un Strike',
      'MAKE_ADMIN': 'Nombró a un nuevo Administrador',
      'REMOVE_ADMIN': 'Revocó permisos de Administrador',
      'BAN_USER': 'Baneó (Shadowban) a un usuario',
      'UNBAN_USER': 'Levantó el baneo a un usuario',
      'UPDATE_CATEGORY': 'Corrigió la especie de un insecto',
      'UPDATE_DANGER_LEVEL': 'Modificó el nivel de peligro de un insecto'
    };
    return dict[action] || action;
  };

  if (cargando) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Cargando registros...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4 fade-in-up">
      <div className="d-flex flex-column mb-4">
        <h2 className="mb-0 fw-bold text-dark d-flex align-items-center">
          <ShieldAlert className="me-2 text-danger" size={28} />
          Registro de Auditoría
        </h2>
        <p className="text-muted mb-0">Monitorea las acciones realizadas por los administradores de iBichos para evitar el abuso de poder.</p>
      </div>

      <div className="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th className="ps-4 text-secondary">Fecha y Hora</th>
                <th className="text-secondary">Acción Realizada</th>
                <th className="text-secondary">Administrador</th>
                <th className="text-secondary">Afectado (Objetivo)</th>
              </tr>
            </thead>
            <tbody>
              {logs.length > 0 ? (
                logs.map((log) => (
                  <tr key={log.id} className="border-bottom">
                    <td className="ps-4 py-3">
                      <div className="d-flex flex-column">
                        <span className="fw-bold text-dark">{log.timestamp.toLocaleDateString('es-CL')}</span>
                        <small className="text-muted">{log.timestamp.toLocaleTimeString('es-CL')}</small>
                      </div>
                    </td>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        {getActionIcon(log.action)}
                        <span className="fw-semibold text-dark">{getActionLabel(log.action)}</span>
                      </div>
                    </td>
                    <td>
                      <div className="d-flex flex-column">
                        <span className="fw-bold text-dark">{log.adminEmail || 'Admin Desconocido'}</span>
                        <small className="text-muted font-monospace" style={{fontSize: '0.65rem'}}>{log.adminId}</small>
                      </div>
                    </td>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        {log.targetType === 'USER' ? <User size={16} className="text-muted" /> : <Image size={16} className="text-muted" />}
                        <span className="text-muted small font-monospace">{log.targetId}</span>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="text-center py-5 text-muted">
                    <ShieldAlert size={48} className="text-secondary opacity-50 mb-3" />
                    <h5 className="fw-bold">No hay registros de auditoría</h5>
                    <p className="mb-1">Aún no se ha realizado ninguna acción de moderación, o las acciones se hicieron antes de esta actualización.</p>
                    <small className="text-muted d-block mt-2 opacity-75">
                      <em>Nota técnica: Si crees que esto es un error, asegúrate de haber añadido las reglas de seguridad a la colección <code>moderation_logs</code> en Firebase.</em>
                    </small>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

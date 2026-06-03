import { ShieldAlert, User, Image } from 'lucide-react';
import { useAuditoria } from './useAuditoria';
import { getActionIcon, getActionLabel } from './auditoriaUtils';

export default function Auditoria() {
  const { logs, cargando } = useAuditoria();

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
                    <p className="mb-1">Aún no se ha realizado ninguna acción de moderación.</p>
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
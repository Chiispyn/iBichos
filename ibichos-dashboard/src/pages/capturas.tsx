import { useEffect, useState } from 'react';
import { collection, getDocs, doc, updateDoc } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import { CheckCircle, XCircle, Clock, ShieldAlert } from 'lucide-react';

interface Captura {
  id: string;
  imageUrl: string;
  category: string;
  dangerLevel: string;
  confidence: number;
  needsReview: boolean;
  validationStatus: string; // 'PENDING_REVIEW', 'APPROVED', 'REJECTED'
  userId: string;
}

export default function Capturas() {
  const [capturas, setCapturas] = useState<Captura[]>([]);
  const [filtro, setFiltro] = useState<'ALL' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED'>('ALL');
  const [cargando, setCargando] = useState(true);

  const fetchCapturas = async () => {
    try {
      const querySnapshot = await getDocs(collection(db, "captures"));
      const datos: Captura[] = querySnapshot.docs.map(doc => {
        const d = doc.data();
        return {
          id: doc.id,
          imageUrl: d.imageUrl || '',
          category: d.category || 'Desconocido',
          dangerLevel: d.dangerLevel || 'UNKNOWN',
          confidence: d.probability || 0,
          needsReview: d.needsReview || false,
          validationStatus: d.validationStatus || (d.needsReview ? 'PENDING_REVIEW' : (d.probability < 0.40 ? 'REJECTED' : 'APPROVED')),
          userId: d.userId || 'Anónimo'
        };
      });

      // Ordenar: primero las que necesitan review (PENDING_REVIEW)
      datos.sort((a, b) => {
        if (a.validationStatus === 'PENDING_REVIEW' && b.validationStatus !== 'PENDING_REVIEW') return -1;
        if (a.validationStatus !== 'PENDING_REVIEW' && b.validationStatus === 'PENDING_REVIEW') return 1;
        return 0;
      });

      setCapturas(datos);
    } catch (error) {
      console.error("Error al obtener capturas:", error);
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    fetchCapturas();
  }, []);

  const handleModeracion = async (id: string, nuevoEstado: 'APPROVED' | 'REJECTED') => {
    try {
      const capturaRef = doc(db, 'captures', id);
      await updateDoc(capturaRef, {
        validationStatus: nuevoEstado,
        needsReview: false 
      });
      
      setCapturas(prev => prev.map(cap => 
        cap.id === id ? { ...cap, validationStatus: nuevoEstado, needsReview: false } : cap
      ));
    } catch (error) {
      console.error("Error al moderar la captura:", error);
    }
  };

  const capturasFiltradas = filtro === 'ALL' 
    ? capturas 
    : capturas.filter(c => c.validationStatus === filtro);

  const getDangerColor = (level: string) => {
    switch(level) {
      case 'HARMLESS': return 'success';
      case 'VENOMOUS': return 'danger';
      case 'CAUTION': return 'warning';
      default: return 'secondary';
    }
  };

  const traducirPeligro = (level: string) => {
    switch(level) {
      case 'HARMLESS': return 'Inofensivo';
      case 'VENOMOUS': return 'Venenoso';
      case 'CAUTION': return 'Precaución';
      default: return 'Desconocido';
    }
  };

  const traducirCategoria = (cat: string) => {
    const dict: Record<string, string> = { 
      HYMENOPTERA: 'Abejas/Avispas', ARACHNIDA: 'Arácnidos', 
      COLEOPTERA: 'Escarabajos', LEPIDOPTERA: 'Mariposas', 
      DIPTERA: 'Moscas', BLATTODEA: 'Cucarachas', 
      HEMIPTERA: 'Chinches', ORTHOPTERA: 'Grillos/Saltamontes', 
      ODONATA: 'Libélulas', OTHER: 'Otro' 
    };
    return dict[cat] || cat;
  };

  if (cargando) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold text-success">
            <ShieldAlert className="me-2 mb-1" />
            Moderación de Capturas
          </h2>
          <p className="text-muted mb-0">Gestiona y valida la calidad de las fotografías enviadas por los usuarios.</p>
        </div>

        {/* Filtros */}
        <div className="btn-group shadow-sm p-1 bg-white rounded-3">
          <button 
            className={`btn btn-sm px-3 ${filtro === 'ALL' ? 'btn-success active' : 'btn-light'}`}
            onClick={() => setFiltro('ALL')}
          >
            Todas
          </button>
          <button 
            className={`btn btn-sm px-3 ${filtro === 'PENDING_REVIEW' ? 'btn-warning active' : 'btn-light'}`}
            onClick={() => setFiltro('PENDING_REVIEW')}
          >
            Pendientes
          </button>
          <button 
            className={`btn btn-sm px-3 ${filtro === 'APPROVED' ? 'btn-success active' : 'btn-light'}`}
            onClick={() => setFiltro('APPROVED')}
          >
            Aprobadas
          </button>
          <button 
            className={`btn btn-sm px-3 ${filtro === 'REJECTED' ? 'btn-danger active' : 'btn-light'}`}
            onClick={() => setFiltro('REJECTED')}
          >
            Rechazadas
          </button>
        </div>
      </div>

      <div className="row g-4">
        {capturasFiltradas.length === 0 ? (
          <div className="col-12 text-center py-5">
            <div className="bg-white p-5 rounded-4 shadow-sm">
              <Clock size={48} className="text-muted mb-3" />
              <p className="text-muted fs-5 mb-0">No hay capturas en esta categoría.</p>
            </div>
          </div>
        ) : (
          capturasFiltradas.map((cap) => (
            <div key={cap.id} className="col-12 col-md-6 col-lg-4 col-xl-3">
              <div className="card h-100 border-0 shadow-sm rounded-4 overflow-hidden position-relative">
                
                {/* Badge de Estado */}
                <div className="position-absolute top-0 end-0 m-2 z-1">
                  {cap.validationStatus === 'PENDING_REVIEW' ? (
                    <span className="badge bg-warning text-dark shadow-sm"><Clock size={12} className="me-1"/> Revisión</span>
                  ) : cap.validationStatus === 'APPROVED' ? (
                    <span className="badge bg-success shadow-sm"><CheckCircle size={12} className="me-1"/> Aprobada</span>
                  ) : (
                    <span className="badge bg-danger shadow-sm"><XCircle size={12} className="me-1"/> Rechazada</span>
                  )}
                </div>

                <div 
                  style={{ 
                    height: '220px', 
                    backgroundImage: `url(${cap.imageUrl || 'https://via.placeholder.com/300x220?text=Sin+Imagen'})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center'
                  }}
                />

                <div className="card-body">
                  <div className="mb-2">
                    <span className="badge bg-light text-dark border mb-2">{traducirCategoria(cap.category)}</span>
                    <p className="small text-muted mb-0">UID: <code>{cap.userId.substring(0,8)}</code></p>
                  </div>
                  
                  <hr className="my-3 opacity-10" />

                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="small text-secondary">Certeza IA:</span>
                    <span className={`fw-bold ${cap.confidence < 0.4 ? 'text-danger' : cap.confidence < 0.75 ? 'text-warning' : 'text-success'}`}>
                      {(cap.confidence * 100).toFixed(1)}%
                    </span>
                  </div>

                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <span className="small text-secondary">Peligrosidad:</span>
                    <span className={`badge bg-${getDangerColor(cap.dangerLevel)}`}>
                      {traducirPeligro(cap.dangerLevel)}
                    </span>
                  </div>

                  {/* Acciones */}
                  <div className="d-grid gap-2 d-flex">
                    <button 
                      className="btn btn-outline-success btn-sm flex-grow-1 fw-bold"
                      onClick={() => handleModeracion(cap.id, 'APPROVED')}
                      disabled={cap.validationStatus === 'APPROVED'}
                    >
                      Aprobar
                    </button>
                    <button 
                      className="btn btn-outline-danger btn-sm flex-grow-1 fw-bold"
                      onClick={() => handleModeracion(cap.id, 'REJECTED')}
                      disabled={cap.validationStatus === 'REJECTED'}
                    >
                      Rechazar
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

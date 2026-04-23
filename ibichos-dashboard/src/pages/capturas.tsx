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
  status: string; // 'PENDING', 'APPROVED', 'REJECTED'
  userId: string;
}

export default function Capturas() {
  const [capturas, setCapturas] = useState<Captura[]>([]);
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
          confidence: d.probability || 0, // ¡En Android se llama probability!
          needsReview: d.needsReview || false,
          status: d.status || 'PENDING',
          userId: d.userId || 'Anónimo'
        };
      });

      // Ordenar: primero las PENDIENTES o que necesitan review
      datos.sort((a, b) => {
        if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
        if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;
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
        status: nuevoEstado,
        needsReview: false // Ya fue revisada
      });
      
      // Actualizar UI sin recargar
      setCapturas(prev => prev.map(cap => 
        cap.id === id ? { ...cap, status: nuevoEstado, needsReview: false } : cap
      ));
    } catch (error) {
      console.error("Error al moderar la captura:", error);
      alert("Error de conexión al moderar.");
    }
  };

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
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold text-success">
            <ShieldAlert className="me-2 mb-1" />
            Moderación de Capturas
          </h2>
          <p className="text-muted">Revisa las fotografías que la IA no pudo identificar con certeza o pendientes de aprobación.</p>
        </div>
      </div>

      <div className="row g-4">
        {capturas.length === 0 ? (
          <div className="col-12 text-center py-5">
            <p className="text-muted fs-5">No hay capturas registradas en la base de datos.</p>
          </div>
        ) : (
          capturas.map((cap) => (
            <div key={cap.id} className="col-12 col-md-6 col-lg-4 col-xl-3">
              <div className="card h-100 border-0 shadow-sm rounded-4 overflow-hidden">
                
                {/* Imagen (Cloudinary o Firebase Storage) */}
                <div 
                  style={{ 
                    height: '200px', 
                    backgroundImage: `url(${cap.imageUrl || 'https://via.placeholder.com/300x200?text=Sin+Imagen'})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center'
                  }}
                  className="position-relative"
                >
                  {/* Etiqueta de Estado Flotante */}
                  <div className="position-absolute top-0 end-0 m-2">
                    {cap.status === 'PENDING' ? (
                      <span className="badge bg-warning text-dark"><Clock size={12} className="me-1"/> Pendiente</span>
                    ) : cap.status === 'APPROVED' ? (
                      <span className="badge bg-success"><CheckCircle size={12} className="me-1"/> Aprobado</span>
                    ) : (
                      <span className="badge bg-danger"><XCircle size={12} className="me-1"/> Rechazado</span>
                    )}
                  </div>
                </div>

                <div className="card-body">
                  <h5 className="fw-bold mb-1">{traducirCategoria(cap.category)}</h5>
                  <p className="small text-muted mb-3">ID Usuario: {cap.userId.substring(0,8)}...</p>
                  
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="small text-secondary">Certeza IA:</span>
                    <span className="fw-bold">{(cap.confidence * 100).toFixed(1)}%</span>
                  </div>

                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <span className="small text-secondary">Peligrosidad:</span>
                    <span className={`badge bg-${getDangerColor(cap.dangerLevel)}`}>
                      {traducirPeligro(cap.dangerLevel)}
                    </span>
                  </div>

                  {/* Acciones de Moderación */}
                  <div className="d-grid gap-2 d-flex mt-4">
                    <button 
                      className="btn btn-outline-success flex-grow-1"
                      onClick={() => handleModeracion(cap.id, 'APPROVED')}
                      disabled={cap.status === 'APPROVED'}
                    >
                      <CheckCircle size={18} className="me-1" /> Aprobar
                    </button>
                    <button 
                      className="btn btn-outline-danger flex-grow-1"
                      onClick={() => handleModeracion(cap.id, 'REJECTED')}
                      disabled={cap.status === 'REJECTED'}
                    >
                      <XCircle size={18} className="me-1" /> Rechazar
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

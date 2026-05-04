import { useEffect, useState } from 'react';
import { db } from '../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';
import { useAuth } from '../context/authcontext';
import { UserPlus, ImagePlus, AlertCircle, Sparkles, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Principal() {
  const { username } = useAuth();
  const navigate = useNavigate();
  const [statsDia, setStatsDia] = useState({ nuevosUsuarios: 0, capturasHoy: 0, pendientesHoy: 0 });
  const [cargando, setCargando] = useState(true);

  // Obtener fecha actual en formato dd/mm/aaaa
  const hoy = new Date();
  const opcionesFecha: Intl.DateTimeFormatOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  const fechaFormateada = hoy.toLocaleDateString('es-CL', opcionesFecha);

  useEffect(() => {
    const fetchMetricasDiarias = async () => {
      try {
        const hoyString = hoy.toLocaleDateString('es-CL');
        let nuevosUsuarios = 0;
        let capturasHoy = 0;
        let pendientesHoy = 0;

        const usersSnap = await getDocs(collection(db, 'users'));
        usersSnap.forEach(doc => {
          const data = doc.data();
          if (data.createdAt) {
            const fechaReg = new Date(data.createdAt.toMillis()).toLocaleDateString('es-CL');
            if (fechaReg === hoyString) nuevosUsuarios++;
          }
        });

        const capturesSnap = await getDocs(collection(db, 'captures'));
        capturesSnap.forEach(doc => {
          const data = doc.data();
          if (data.timestamp) {
            const fechaCap = new Date(data.timestamp.toMillis()).toLocaleDateString('es-CL');
            if (fechaCap === hoyString) {
              // Solo contamos como "Capturas Hoy" las que ya están aprobadas (auto o manual)
              const status = data.validationStatus || data.status;
              if (status === 'APPROVED') {
                capturasHoy++;
              }
              
              // El contador de "Atención Requerida" sigue captando las que necesitan revisión
              if (data.needsReview || status === 'PENDING_REVIEW' || status === 'PENDING') {
                pendientesHoy++;
              }
            }
          }
        });

        setStatsDia({ nuevosUsuarios, capturasHoy, pendientesHoy });
      } catch (error) {
        console.error("Error obteniendo métricas del día:", error);
      } finally {
        setCargando(false);
      }
    };

    fetchMetricasDiarias();
  }, []);

  return (
    <div className="container-fluid py-4">
      {/* Saludo Banner */}
      <div className="bg-success text-white rounded-4 p-5 mb-5 shadow-sm position-relative overflow-hidden">
        <div className="position-relative" style={{ zIndex: 1 }}>
          <h1 className="fw-bold mb-2">¡Hola de nuevo, <span className="text-warning">{username}</span>! 👋</h1>
          <p className="fs-5 mb-0 text-white-50 text-capitalize">{fechaFormateada}</p>
        </div>
        <Sparkles className="position-absolute text-white opacity-25" size={150} style={{ right: '-20px', top: '-20px' }} />
      </div>

      <h4 className="fw-bold mb-4">Resumen de Actividad de Hoy</h4>

      {cargando ? (
        <div className="d-flex justify-content-center my-5">
          <div className="spinner-border text-success" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : (
        <div className="row g-4 fade-in-up">
          {/* Card: Nuevos Usuarios */}
          <div className="col-md-4">
            <div 
              className="card ibichos-card h-100 p-2 border-start border-4 border-primary"
              style={{ cursor: 'pointer' }}
              onClick={() => navigate('/usuarios')}
            >
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h6 className="text-muted fw-bold text-uppercase mb-0">Nuevos Usuarios</h6>
                  <div className="bg-primary bg-opacity-10 p-2 rounded">
                    <UserPlus size={24} className="text-primary" />
                  </div>
                </div>
                <h2 className="fw-bold mb-1">{statsDia.nuevosUsuarios}</h2>
                <div className="d-flex justify-content-between align-items-center mt-3">
                  <small className="text-primary fw-bold">Gestionar usuarios</small>
                  <ChevronRight size={16} className="text-primary" />
                </div>
              </div>
            </div>
          </div>

          {/* Card: Aprobadas Hoy */}
          <div className="col-md-4">
            <div 
              className="card ibichos-card h-100 p-2 border-start border-4 border-success"
              style={{ cursor: 'pointer' }}
              onClick={() => navigate('/capturas')}
            >
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h6 className="text-muted fw-bold text-uppercase mb-0">Aprobadas Hoy</h6>
                  <div className="bg-success bg-opacity-10 p-2 rounded">
                    <ImagePlus size={24} className="text-success" />
                  </div>
                </div>
                <h2 className="fw-bold mb-1">{statsDia.capturasHoy}</h2>
                <div className="d-flex justify-content-between align-items-center mt-3">
                  <small className="text-success fw-bold">Ver galería de fotos</small>
                  <ChevronRight size={16} className="text-success" />
                </div>
              </div>
            </div>
          </div>

          {/* Card: Atención Requerida */}
          <div className="col-md-4">
            <div 
              className="card ibichos-card h-100 p-2 border-start border-4 border-warning"
              style={{ cursor: 'pointer' }}
              onClick={() => navigate('/capturas')}
            >
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h6 className="text-muted fw-bold text-uppercase mb-0">Atención Requerida</h6>
                  <div className="bg-warning bg-opacity-10 p-2 rounded">
                    <AlertCircle size={24} className="text-warning" />
                  </div>
                </div>
                <h2 className="fw-bold mb-1">{statsDia.pendientesHoy}</h2>
                <div className="d-flex justify-content-between align-items-center mt-3">
                  <small className="text-warning fw-bold text-dark">Moderar capturas dudosas</small>
                  <ChevronRight size={16} className="text-warning" />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

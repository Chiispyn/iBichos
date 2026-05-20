import { useEffect, useState } from 'react';
import { collection, onSnapshot, doc, updateDoc, deleteDoc, increment, setDoc } from 'firebase/firestore';
import { db } from '../config/firebaseConfig';
import { CheckCircle, XCircle, Clock, ShieldAlert, Trash2 } from 'lucide-react';
import { useAuth } from '../context/authcontext';

interface Captura {
  id: string;
  imageUrl: string;
  category: string;
  dangerLevel: string;
  confidence: number;
  needsReview: boolean;
  status: string;
  userId: string;
  userDisplayName?: string;
  userEmail?: string;
  insectName?: string;
  scientificName?: string;
  lat?: number;
  lng?: number;
  moderatedBy?: string;
  moderatorEmail?: string;
}

export default function Capturas() {
  const { user } = useAuth();
  const [capturas, setCapturas] = useState<Captura[]>([]);
  const [filtro, setFiltro] = useState<'PENDING_REVIEW' | 'REJECTED'>('PENDING_REVIEW');
  const [cargando, setCargando] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [selectedImg, setSelectedImg] = useState<string | null>(null);
  const [userMap, setUserMap] = useState<Record<string, { name: string; email: string }>>({});

  useEffect(() => {
    // Cargar mapa de usuarios una sola vez
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
    // Escucha en tiempo real de la colección "captures"
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
            moderatorEmail: d.moderatorEmail
          };
        })
        .filter(cap => cap.status !== 'DELETED');

      // Ordenar: Pendientes primero
      datos.sort((a, b) => {
        if (a.status === 'PENDING_REVIEW' && b.status !== 'PENDING_REVIEW') return -1;
        if (a.status !== 'PENDING_REVIEW' && b.status === 'PENDING_REVIEW') return 1;
        return 0;
      });

      setCapturas(datos);
      setCargando(false);
    }, (error) => {
      console.error("Error en tiempo real (Capturas):", error);
      setCargando(false);
    });

    return () => unsubscribe();
  }, []);

  // --- LÓGICA DE MODERACIÓN Y PENALIZACIÓN ---
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

  // --- LÓGICA DE BORRADO LÓGICO (Ocultar sin perder datos) ---
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
        console.warn("No se pudo guardar el log (¿Faltan reglas en Firestore?):", e);
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

  // --- FILTRADO Y TRADUCCIONES ---
  const capturasFiltradas = capturas.filter(c => c.status === filtro);

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

  // --- VISTA DE CARGA ---
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
      {/* CABECERA Y FILTROS */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold text-success">
            <ShieldAlert className="me-2 mb-1" />
            Moderación de Capturas
          </h2>
          <p className="text-muted mb-0">Valida la autenticidad de las capturas y gestiona la seguridad del ecosistema.</p>
        </div>

        {/* Filtros de Trabajo (Solo Pendientes y Rechazadas) */}
        <div className="btn-group shadow-sm p-1 bg-white rounded-3">
          <button
            className={`btn btn-sm px-4 fw-bold ${filtro === 'PENDING_REVIEW' ? 'btn-warning active shadow-sm' : 'btn-light'}`}
            onClick={() => setFiltro('PENDING_REVIEW')}
          >
            <Clock size={16} className="me-2 mb-1" />
            Pendientes de Revisión
          </button>
          <button
            className={`btn btn-sm px-4 fw-bold ${filtro === 'REJECTED' ? 'btn-danger active shadow-sm' : 'btn-light'}`}
            onClick={() => setFiltro('REJECTED')}
          >
            <XCircle size={16} className="me-2 mb-1" />
            Rechazadas / Ocultas
          </button>
        </div>
      </div>

      {/* GRILLA DE CAPTURAS */}
      <div className="row g-4">
        {capturasFiltradas.length === 0 ? (
          <div className="col-12 text-center py-5">
            <div className="bg-white p-5 rounded-4 shadow-sm">
              <Clock size={48} className="text-muted mb-3" />
              <p className="text-muted fs-5 mb-0">No hay capturas que mostrar aquí.</p>
            </div>
          </div>
        ) : (
          capturasFiltradas.map((cap) => {
            // HEURÍSTICA ANTI-FRAUDE: Detectamos si hay muchas fotos en la misma coordenada
            const mismasCoord = capturas.filter(c =>
              c.userId === cap.userId &&
              c.lat === cap.lat &&
              c.lng === cap.lng &&
              cap.lat !== undefined
            ).length;
            const esSospechoso = mismasCoord >= 3;

            return (
              <div key={cap.id} className="col-12 col-md-6 col-lg-4 col-xl-3 fade-in-up">
                <div className={`card h-100 ibichos-card position-relative ${esSospechoso ? 'border border-warning border-2' : ''}`}>

                  {/* Contenedor de Imagen y Banner de Fraude */}
                  <div className="position-relative" style={{ cursor: 'zoom-in' }} onClick={() => setSelectedImg(cap.imageUrl)}>
                    <div className="ibichos-img-container" style={{ backgroundImage: `url(${cap.imageUrl || 'https://via.placeholder.com/300x220?text=Sin+Imagen'})` }} />

                    <div className="position-absolute top-0 end-0 m-2 z-1 d-flex flex-column gap-1 align-items-end">
                      {cap.status === 'PENDING_REVIEW' ? (
                        <span className="badge bg-warning text-dark shadow-sm"><Clock size={12} className="me-1" /> Revisión</span>
                      ) : cap.status === 'APPROVED' ? (
                        <span className="badge bg-success shadow-sm"><CheckCircle size={12} className="me-1" /> Aprobada</span>
                      ) : (
                        <>
                          <span className="badge bg-danger shadow-sm"><XCircle size={12} className="me-1" /> Rechazada</span>
                        </>
                      )}
                    </div>

                    <button
                      className="position-absolute top-0 start-0 m-2 z-1 btn btn-dark btn-sm bg-opacity-50 border-0 rounded-circle p-1"
                      onClick={(e) => { e.stopPropagation(); openModal(cap.id); }}
                    >
                      <Trash2 size={16} className="text-white" />
                    </button>

                    {/* ALERTA DE POSIBLE TRAMPA (SCREEN/BOOK) - Ahora en la parte inferior de la foto */}
                    {esSospechoso && (
                      <div className="suspicion-banner">
                        ⚠️ POSIBLE FRAUDE ({mismasCoord} en misma ubicación)
                      </div>
                    )}
                  </div>

                  <div className="card-body">
                    {/* INFO DE LA IA */}
                    <div className="mb-3">
                      <h6 className="mb-0 fw-bold">{cap.insectName || 'Insecto Desconocido'}</h6>
                      <small className="text-muted fst-italic">{cap.scientificName || 'Especie no identificada'}</small>
                    </div>

                    {/* SELECTOR DE CATEGORÍA (CORRECCIÓN DE IA) */}
                    <div className="mb-3">
                      <label className="small text-muted mb-1 d-block">Categoría (Corregir si es necesario):</label>
                      <select
                        className="form-select form-select-sm border-success bg-light fw-bold"
                        value={cap.category}
                        onChange={(e) => handleModeracion(cap.id, cap.status as any, e.target.value)}
                      >
                        <option value="ARACHNID">Arácnido 🕷️</option>
                        <option value="COLEOPTERA">Coleóptero 🐞</option>
                        <option value="LEPIDOPTERA">Lepidóptero 🦋</option>
                        <option value="HYMENOPTERA">Himenóptero 🐝</option>
                        <option value="OTHER">Otro ❓</option>
                      </select>
                      <p className="small text-muted mt-2 mb-2">
                        👤 
                        {userMap[cap.userId]
                          ? <><strong>{userMap[cap.userId].name}</strong> <span className="text-secondary">({userMap[cap.userId].email})</span></>
                          : <code>{cap.userId.substring(0, 10)}...</code>
                        }
                      </p>
                      
                      {cap.status !== 'PENDING_REVIEW' && (
                        <p className="small text-muted mt-1 mb-0 border-top pt-2">
                          <span className="fw-bold">{cap.status === 'APPROVED' ? '✅ Moderado por: ' : '❌ Rechazado por: '}</span>
                          {cap.moderatedBy ? (cap.moderatorEmail || 'Admin') : 'IA'}
                        </p>
                      )}
                    </div>

                    <hr className="my-3 opacity-10" />

                    {/* MÉTRICAS DE IDENTIFICACIÓN */}
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <span className="small text-secondary">Confianza IA:</span>
                      <span className={`fw-bold ${cap.confidence < 0.4 ? 'text-danger' : cap.confidence < 0.75 ? 'text-warning' : 'text-success'}`}>
                        {(cap.confidence * 100).toFixed(1)}%
                      </span>
                    </div>

                    <div className="d-flex justify-content-between align-items-center mb-4">
                      <span className="small text-secondary">Riesgo:</span>
                      <span className={`badge bg-${getDangerColor(cap.dangerLevel)}`}>
                        {traducirPeligro(cap.dangerLevel)}
                      </span>
                    </div>

                    {/* ACCIONES DE MODERACIÓN */}
                    <div className="d-grid gap-2 d-flex">
                      <button
                        className={`btn btn-sm flex-grow-1 fw-bold ${cap.status === 'APPROVED' ? 'btn-success' : 'btn-outline-success'}`}
                        onClick={() => handleModeracion(cap.id, 'APPROVED')}
                        disabled={cap.status === 'APPROVED'}
                      >
                        {cap.status === 'APPROVED' ? 'Aprobada' : 'Aprobar'}
                      </button>
                      <button
                        className={`btn btn-sm flex-grow-1 fw-bold ${cap.status === 'REJECTED' ? 'btn-danger' : 'btn-outline-danger'}`}
                        onClick={() => handleModeracion(cap.id, 'REJECTED')}
                        disabled={cap.status === 'REJECTED'}
                      >
                        {cap.status === 'REJECTED' ? 'Rechazada' : 'Rechazar'}
                      </button>
                    </div>

                    {/* REPORTE DE FRAUDE (STRIKE MANUAL) */}
                    {cap.status !== 'APPROVED' && (
                      <button
                        className="btn btn-dark btn-sm w-100 mt-2 fw-bold"
                        onClick={() => handleModeracion(cap.id, 'REJECTED', undefined, true)}
                      >
                        <ShieldAlert size={14} className="me-1" />
                        Reportar Fraude (Strike)
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* MODAL DE ZOOM DE IMAGEN (LIGHTBOX) */}
      {selectedImg && (
        <>
          <div className="modal-backdrop fade show ibichos-modal-overlay" />
          <div className="modal d-block" tabIndex={-1} style={{ zIndex: 1050 }} onClick={() => setSelectedImg(null)}>
            <div className="modal-dialog modal-dialog-centered modal-lg" onClick={e => e.stopPropagation()}>
              <div className="modal-content ibichos-modal-content bg-dark border-0">
                <div className="modal-header border-0 pb-0">
                  <button type="button" className="btn-close btn-close-white ms-auto" onClick={() => setSelectedImg(null)}></button>
                </div>
                <div className="modal-body p-2 text-center">
                  <img
                    src={selectedImg}
                    className="img-fluid rounded shadow-lg"
                    style={{ maxHeight: '80vh', objectFit: 'contain' }}
                    alt="Zoom de insecto"
                  />
                </div>
                <div className="modal-footer border-0 pt-0 justify-content-center">
                  <p className="text-white-50 small mb-2">Vista de alta resolución</p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* MODAL DE CONFIRMACIÓN (DISEÑO PREMIUM) */}
      {showModal && (
        <>
          <div className="modal-backdrop fade show ibichos-modal-overlay" />
          <div className="modal d-block" tabIndex={-1} style={{ zIndex: 1050 }} onClick={() => setShowModal(false)}>
            <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
              <div className="modal-content ibichos-modal-content bg-white">
                <div className="modal-header bg-danger text-white border-0 py-3">
                  <h5 className="modal-title fw-bold d-flex align-items-center">
                    <ShieldAlert className="me-2" />
                    Confirmar Acción
                  </h5>
                  <button type="button" className="btn-close btn-close-white" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4 text-center">
                  <div className="bg-danger bg-opacity-10 p-4 rounded-circle d-inline-block mb-3">
                    <Trash2 size={48} className="text-danger" />
                  </div>
                  <h4 className="fw-bold mb-2">¿Ocultar captura?</h4>
                  <p className="text-muted mb-0">La foto desaparecerá de la galería, pero se mantendrá en los registros históricos para no alterar las estadísticas de la IA.</p>
                </div>
                <div className="modal-footer border-0 p-3 bg-light d-flex">
                  <button type="button" className="btn btn-light rounded-3 flex-grow-1 fw-bold" onClick={() => setShowModal(false)}>Cancelar</button>
                  <button type="button" className="btn btn-danger rounded-3 flex-grow-1 fw-bold" onClick={handleEliminar}>Sí, Ocultar</button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

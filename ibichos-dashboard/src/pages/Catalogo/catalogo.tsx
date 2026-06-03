// src/pages/Catalogo/Catalogo.tsx
import { Search, Bug, MapPin, ShieldCheck, Trash2, ShieldAlert } from 'lucide-react';
import { useCatalogo } from './useCatalogo';

export default function Catalogo() {
  const {
    cargando,
    busqueda, setBusqueda,
    selectedImg, setSelectedImg,
    showModal, setShowModal,
    userMap,
    filtered,
    handleUpdate,
    handleReject,
    openDeleteModal,
    getDangerBadge
  } = useCatalogo();

  if (cargando) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Cargando catálogo...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* CABECERA */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold text-success">
            <Bug className="me-2 mb-1" />
            Catálogo de Especies iBichos
          </h2>
          <p className="text-muted mb-0">Explora todos los avistamientos verificados por la comunidad y el equipo experto.</p>
        </div>

        {/* Buscador */}
        <div className="position-relative" style={{ maxWidth: '400px', width: '100%' }}>
          <Search className="position-absolute top-50 start-0 translate-middle-y ms-3 text-muted" size={18} />
          <input 
            type="text" 
            className="form-control ps-5 border-success shadow-sm rounded-3" 
            placeholder="Buscar por categoría o ID..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>
      </div>

      {/* GRILLA DE GALERÍA */}
      <div className="row g-4">
        {filtered.length === 0 ? (
          <div className="col-12 text-center py-5">
            <div className="bg-white p-5 rounded-4 shadow-sm border border-dashed border-2">
              <Bug size={48} className="text-muted mb-3 opacity-25" />
              <p className="text-muted fs-5 mb-0">No se encontraron especies con ese criterio.</p>
            </div>
          </div>
        ) : (
          filtered.map((cap) => (
            <div key={cap.id} className="col-12 col-md-6 col-lg-4 col-xl-3">
              <div className="card h-100 ibichos-card overflow-hidden border-0 shadow-sm">
                {/* Imagen */}
                <div 
                  className="position-relative" 
                  style={{ cursor: 'pointer', height: '220px' }}
                  onClick={() => setSelectedImg(cap.imageUrl)}
                >
                  <img 
                    src={cap.imageUrl || 'https://via.placeholder.com/300x220?text=Sin+Imagen'} 
                    className="w-100 h-100 object-fit-cover" 
                    alt={cap.category} 
                  />
                  <div className="position-absolute bottom-0 start-0 w-100 p-3 bg-gradient-dark">
                    <span className="badge bg-success shadow-sm d-inline-flex align-items-center">
                      <ShieldCheck size={12} className="me-1" /> Verificado
                    </span>
                  </div>
                </div>

                {/* Detalles */}
                <div className="card-body">
                  {/* INFO DE LA IA */}
                  <div className="mb-3">
                    <h6 className="mb-0 fw-bold">{cap.insectName || 'Insecto Desconocido'}</h6>
                    <small className="text-muted fst-italic">{cap.scientificName || 'Especie no identificada'}</small>
                  </div>

                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <div className="flex-grow-1 me-2">
                      <select 
                        className="form-select form-select-sm border-0 fw-bold p-0 bg-transparent text-dark fs-5"
                        value={cap.category}
                        onChange={(e) => handleUpdate(cap.id, { category: e.target.value })}
                        style={{ cursor: 'pointer' }}
                      >
                        <option value="ARACHNID">Arácnido 🕷️</option>
                        <option value="COLEOPTERA">Coleóptero 🐞</option>
                        <option value="LEPIDOPTERA">Lepidóptero 🦋</option>
                        <option value="HYMENOPTERA">Himenóptero 🐝</option>
                        <option value="OTHER">Otro ❓</option>
                      </select>
                    </div>
                    <select 
                      className={`form-select form-select-sm border-0 rounded-pill px-2 fw-bold text-white ${getDangerBadge(cap.dangerLevel)}`}
                      value={cap.dangerLevel}
                      onChange={(e) => handleUpdate(cap.id, { dangerLevel: e.target.value })}
                      style={{ cursor: 'pointer', width: 'auto', fontSize: '0.7rem' }}
                    >
                      <option value="HARMLESS" className="bg-white text-dark">Inofensivo</option>
                      <option value="CAUTION" className="bg-white text-dark">Precaución</option>
                      <option value="VENOMOUS" className="bg-white text-dark">Venenoso</option>
                      <option value="UNKNOWN" className="bg-white text-dark">Desconocido</option>
                    </select>
                  </div>
                  
                  <div className="d-flex align-items-center text-muted small mb-3">
                    <MapPin size={14} className="me-1" />
                    <span>
                      {userMap[cap.userId]
                        ? <><strong className="text-dark">{userMap[cap.userId].name}</strong> · <span>{userMap[cap.userId].email}</span></>
                        : cap.userId !== 'Anónimo' ? cap.userId.substring(0, 10) + '...' : 'Anónimo'
                      }
                    </span>
                  </div>

                  <div className="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
                    <div className="small text-muted">
                      Confianza: <span className="fw-bold text-success">{(cap.confidence * 100).toFixed(0)}%</span>
                    </div>
                    <button 
                      className="btn btn-outline-danger btn-sm border-0 p-1 rounded-circle"
                      onClick={(e) => { e.stopPropagation(); openDeleteModal(cap.id); }}
                      title="Rechazar y quitar del catálogo"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* LIGHTBOX */}
      {selectedImg && (
        <>
          <div className="modal-backdrop fade show ibichos-modal-overlay" />
          <div className="modal d-block" tabIndex={-1} onClick={() => setSelectedImg(null)}>
            <div className="modal-dialog modal-dialog-centered modal-lg">
              <div className="modal-content bg-transparent border-0">
                <div className="modal-header border-0 p-0 mb-2">
                  <button type="button" className="btn-close btn-close-white ms-auto" onClick={() => setSelectedImg(null)}></button>
                </div>
                <img src={selectedImg} className="img-fluid rounded-4 shadow-lg" style={{ maxHeight: '85vh' }} alt="Verificado" />
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
                    <ShieldAlert className="me-2" size={20} />
                    Quitar del Catálogo
                  </h5>
                  <button type="button" className="btn-close btn-close-white" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4 text-center">
                  <div className="bg-danger bg-opacity-10 p-4 rounded-circle d-inline-block mb-3">
                    <Trash2 size={48} className="text-danger" />
                  </div>
                  <h4 className="fw-bold mb-2">¿Mover a rechazadas?</h4>
                  <p className="text-muted mb-0">Esta especie dejará de ser pública en el catálogo y volverá a la lista de rechazadas para revisión.</p>
                </div>
                <div className="modal-footer border-0 p-3 bg-light d-flex">
                  <button type="button" className="btn btn-light rounded-3 flex-grow-1 fw-bold" onClick={() => setShowModal(false)}>Cancelar</button>
                  <button type="button" className="btn btn-danger rounded-3 flex-grow-1 fw-bold" onClick={handleReject}>Sí, Quitar</button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
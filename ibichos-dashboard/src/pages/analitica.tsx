import { useEffect, useState } from 'react';
import { db } from '../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, AreaChart, Area
} from 'recharts';
import { Activity, Users, Camera, ShieldAlert, Clock, Smartphone, Bug, ShieldCheck, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const COLORS = ['#3DDC84', '#F4B400', '#DB4437', '#4285F4', '#9C27B0', '#00BCD4'];
const DANGER_COLORS = { 'Inofensivo': '#3DDC84', 'Precaución': '#F4B400', 'Venenoso': '#DB4437', 'Desconocido': '#9E9E9E' };

export default function Analitica() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('actividad');
  const [loading, setLoading] = useState(true);
  
  // --- ESTADOS DE MÉTRICAS ---
  const [stats, setStats] = useState({ totalUsers: 0, totalCaptures: 0, pendingReview: 0, totalSessions: 0, totalMinutes: 0 });
  const [levelData, setLevelData] = useState<any[]>([]);
  const [categoryData, setCategoryData] = useState<any[]>([]);
  const [dangerData, setDangerData] = useState<any[]>([]);
  const [sessionsData, setSessionsData] = useState<any[]>([]);
  const [validationData, setValidationData] = useState<any[]>([]);
  const [monthlySessionsData, setMonthlySessionsData] = useState<any[]>([]);
  const [monthlyCapturesData, setMonthlyCapturesData] = useState<any[]>([]);

  // --- FUNCIONES DE APOYO (Traducción de Enums de Firestore/Kotlin) ---
  const traducirNivel = (lvl: string) => {
    const dict: Record<string, string> = { CASUAL: 'Casual', AMATEUR: 'Amateur', EXPLORER: 'Explorador', ENTOMOLOGIST: 'Entomólogo', BUG_MASTER: 'Maestro de Bichos' };
    return dict[lvl] || lvl;
  };

  const traducirCategoria = (cat: string) => {
    const dict: Record<string, string> = { HYMENOPTERA: 'Abejas/Avispas', ARACHNIDA: 'Arácnidos', COLEOPTERA: 'Escarabajos', LEPIDOPTERA: 'Mariposas', DIPTERA: 'Moscas', BLATTODEA: 'Cucarachas', HEMIPTERA: 'Chinches', ORTHOPTERA: 'Grillos/Saltamontes', ODONATA: 'Libélulas', OTHER: 'Otro' };
    return dict[cat] || cat;
  };

  const traducirPeligro = (danger: string) => {
    const dict: Record<string, string> = { HARMLESS: 'Inofensivo', VENOMOUS: 'Venenoso', CAUTION: 'Precaución', UNKNOWN: 'Desconocido' };
    return dict[danger] || danger;
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // 1. PROCESAMIENTO DE USUARIOS Y LIGAS
        const usersSnap = await getDocs(collection(db, 'users'));
        let totalUsers = 0;
        const levelCounts: Record<string, number> = {};
        usersSnap.forEach(doc => {
          totalUsers++;
          const data = doc.data();
          const level = traducirNivel(data.gamification?.level || 'CASUAL');
          levelCounts[level] = (levelCounts[level] || 0) + 1;
        });

        // 2. PROCESAMIENTO DE CAPTURAS E INTELIGENCIA ARTIFICIAL
        const capturesSnap = await getDocs(collection(db, 'captures'));
        let totalCaptures = 0;
        let pendingReview = 0;
        const catCounts: Record<string, number> = {};
        const dangerCounts: Record<string, number> = {};
        const validationCounts: Record<string, number> = { 'Aprobadas': 0, 'Rechazadas': 0, 'Pendientes': 0 };
        const monthlyCapturesMap: Record<string, number> = {};

        capturesSnap.forEach(doc => {
          totalCaptures++;
          const data = doc.data();
          if (data.needsReview) pendingReview++;
          
          // Agrupación por categoría (Biodiversidad)
          const cat = traducirCategoria(data.category || 'OTHER');
          catCounts[cat] = (catCounts[cat] || 0) + 1;

          // Agrupación por riesgo (Salud Pública)
          const danger = traducirPeligro(data.dangerLevel || 'UNKNOWN');
          dangerCounts[danger] = (dangerCounts[danger] || 0) + 1;

          // Cálculo de Eficacia IA: Comparamos estado de validación (Incluyendo las ocultas/borradas lógicamente)
          if (data.validationStatus === 'REJECTED' || data.validationStatus === 'DELETED' || data.probability < 0.40) {
            validationCounts['Rechazadas']++;
          } else if (data.validationStatus === 'PENDING_REVIEW' || data.needsReview) {
            validationCounts['Pendientes']++;
          } else {
            validationCounts['Aprobadas']++;
          }

          // Timeline Mensual de Capturas
          if (data.timestamp) {
             const dateObj = data.timestamp.toDate ? data.timestamp.toDate() : new Date(data.timestamp);
             const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
             const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);
             monthlyCapturesMap[monthFormatted] = (monthlyCapturesMap[monthFormatted] || 0) + 1;
          }
        });

        // 3. PROCESAMIENTO DE SESIONES (Engagement)
        const sessionsSnap = await getDocs(collection(db, 'sessions'));
        let totalSessions = 0;
        let totalMinutes = 0;
        const timelineData: Record<string, number> = {};
        const monthlySessionsMap: Record<string, number> = {};

        sessionsSnap.forEach(doc => {
          totalSessions++;
          const data = doc.data();
          const duration = data.durationMinutes || 0;
          totalMinutes += duration;
          
          if (data.startedAt) {
            const dateObj = new Date(data.startedAt.toMillis());
            const dateStr = dateObj.toLocaleDateString('es-CL', { month: 'short', day: 'numeric' });
            const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
            const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);
            
            timelineData[dateStr] = (timelineData[dateStr] || 0) + duration;
            monthlySessionsMap[monthFormatted] = (monthlySessionsMap[monthFormatted] || 0) + duration;
          }
        });

        // --- PREPARACIÓN DE DATOS PARA RECHARTS ---
        setStats({ totalUsers, totalCaptures, pendingReview, totalSessions, totalMinutes });
        setLevelData(Object.entries(levelCounts).map(([name, value]) => ({ name, value })));
        setCategoryData(Object.entries(catCounts).map(([name, value]) => ({ name, value })));
        setDangerData(Object.entries(dangerCounts).map(([name, value]) => ({ name, value })));
        setValidationData(Object.entries(validationCounts).map(([name, value]) => ({ name, value })));
        setSessionsData(Object.entries(timelineData).map(([name, minutos]) => ({ name, minutos })));
        setMonthlySessionsData(Object.entries(monthlySessionsMap).map(([name, minutos]) => ({ name, minutos })));
        setMonthlyCapturesData(Object.entries(monthlyCapturesMap).map(([name, capturas]) => ({ name, capturas })));

      } catch (error) {
        console.error("Error al procesar métricas:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Analizando datos...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      <h2 className="mb-4 fw-bold text-success">
        <Activity className="me-2 mb-1" />
        Panel de Inteligencia iBichos
      </h2>

      {/* Tabs Responsivos */}
      <ul className="nav nav-pills mb-4 border-bottom pb-3 d-flex flex-column flex-md-row gap-2">
        <li className="nav-item">
          <button className={`nav-link w-100 ${activeTab === 'actividad' ? 'active bg-success shadow' : 'text-success bg-white border'}`} onClick={() => setActiveTab('actividad')} style={{ fontWeight: 'bold' }}>
            <Activity size={18} className="me-2 mb-1" /> Actividad y Usuarios
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link w-100 ${activeTab === 'biodiversidad' ? 'active bg-success shadow' : 'text-success bg-white border'}`} onClick={() => setActiveTab('biodiversidad')} style={{ fontWeight: 'bold' }}>
            <Bug size={18} className="me-2 mb-1" /> Biodiversidad
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link w-100 ${activeTab === 'moderacion' ? 'active bg-success shadow' : 'text-success bg-white border'}`} onClick={() => setActiveTab('moderacion')} style={{ fontWeight: 'bold' }}>
            <ShieldCheck size={18} className="me-2 mb-1" /> Eficacia IA y Moderación
          </button>
        </li>
      </ul>

      {/* ---------------- PESTAÑA ACTIVIDAD (Engagement de Usuario) ---------------- */}
      {activeTab === 'actividad' && (
        <div className="tab-content fade-in-up">
          <div className="row g-4 mb-4">
            {/* Tarjeta Clicable: Usuarios */}
            <div className="col-md-4">
              <div className="card ibichos-card h-100 shadow-sm" style={{ cursor: 'pointer' }} onClick={() => navigate('/usuarios')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-3">
                    <div className="bg-success bg-opacity-10 p-3 rounded-circle me-3">
                      <Users size={32} className="text-success" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Exploradores Registrados</h6>
                      <h3 className="fw-bold mb-0">{stats.totalUsers}</h3>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mt-2 border-top pt-2">
                    <small className="text-success fw-bold">Gestionar Usuarios</small>
                    <ChevronRight size={14} className="text-success" />
                  </div>
                </div>
              </div>
            </div>
            {/* Métrica: Sesiones */}
            <div className="col-md-4">
              <div className="card ibichos-card h-100">
                <div className="card-body d-flex align-items-center">
                  <div className="bg-info bg-opacity-10 p-3 rounded-circle me-3">
                    <Smartphone size={32} className="text-info" />
                  </div>
                  <div>
                    <h6 className="text-muted mb-1">Sesiones Abiertas</h6>
                    <h3 className="fw-bold mb-0">{stats.totalSessions}</h3>
                  </div>
                </div>
              </div>
            </div>
            {/* Métrica: Tiempo */}
            <div className="col-md-4">
              <div className="card ibichos-card h-100">
                <div className="card-body d-flex align-items-center">
                  <div className="bg-danger bg-opacity-10 p-3 rounded-circle me-3">
                    <Clock size={32} className="text-danger" />
                  </div>
                  <div>
                    <h6 className="text-muted mb-1">Tiempo Total en App</h6>
                    <h3 className="fw-bold mb-0">{stats.totalMinutes} <span className="fs-6 text-muted">min</span></h3>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            {/* Gráfico: Gamificación */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-3">
                <h5 className="fw-bold mb-4 text-dark">Distribución por Ligas</h5>
                <div style={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={levelData}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.3} />
                      <XAxis dataKey="name" tick={{fontSize: 12}} />
                      <YAxis allowDecimals={false} />
                      <Tooltip cursor={{fill: 'rgba(61, 220, 132, 0.1)'}} />
                      <Bar dataKey="value" fill="#3DDC84" radius={[4, 4, 0, 0]} name="Exploradores" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
            {/* Gráfico: Retención Diaria */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-3">
                <h5 className="fw-bold mb-4 text-dark">Uso Diario (Minutos)</h5>
                <div style={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={sessionsData}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.3} />
                      <XAxis dataKey="name" tick={{fontSize: 12}} />
                      <YAxis />
                      <Tooltip />
                      <Line type="monotone" dataKey="minutos" stroke="#9C27B0" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 8 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA BIODIVERSIDAD (Ecosistema) ---------------- */}
      {activeTab === 'biodiversidad' && (
        <div className="tab-content fade-in-up">
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm" style={{ cursor: 'pointer' }} onClick={() => navigate('/capturas')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-2">
                    <div className="bg-primary bg-opacity-10 p-3 rounded-circle me-3">
                      <Camera size={32} className="text-primary" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Inventario Biológico (Total)</h6>
                      <h3 className="fw-bold mb-0">{stats.totalCaptures}</h3>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mt-2 border-top pt-2">
                    <small className="text-primary fw-bold">Ir a Galería de Fotos</small>
                    <ChevronRight size={14} className="text-primary" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            {/* Gráfico: Salud Pública */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-3">
                <h5 className="fw-bold mb-4 text-dark">Riesgos para la Salud</h5>
                <div style={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={dangerData} innerRadius={80} outerRadius={110} paddingAngle={5} dataKey="value">
                        {dangerData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={(DANGER_COLORS as any)[entry.name] || COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="bottom" height={36} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
            {/* Gráfico: Categorías */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-3">
                <h5 className="fw-bold mb-4 text-dark">Especies Identificadas</h5>
                <div style={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={categoryData} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.3} />
                      <XAxis type="number" allowDecimals={false} />
                      <YAxis dataKey="name" type="category" width={90} tick={{fontSize: 11}} />
                      <Tooltip />
                      <Bar dataKey="value" fill="#4285F4" radius={[0, 4, 4, 0]} name="Ejemplares" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA MODERACIÓN (Eficacia IA) ---------------- */}
      {activeTab === 'moderacion' && (
        <div className="tab-content fade-in-up">
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm border-warning" style={{ cursor: 'pointer' }} onClick={() => navigate('/capturas')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-2">
                    <div className="bg-warning bg-opacity-10 p-3 rounded-circle me-3">
                      <ShieldAlert size={32} className="text-warning" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Carga de Trabajo de Moderación</h6>
                      <h3 className="fw-bold mb-0 text-warning">{stats.pendingReview}</h3>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mt-2 border-top pt-2">
                    <small className="text-warning fw-bold">Resolver Pendientes</small>
                    <ChevronRight size={14} className="text-warning" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            <div className="col-lg-12">
              <div className="card ibichos-card p-3">
                <h5 className="fw-bold mb-4 text-dark">Rendimiento del Modelo IA vs Humano</h5>
                <p className="text-muted small mb-4">Este gráfico muestra la proporción de fotos que la IA aprobó directamente vs las que fueron rechazadas o necesitan tu ojo experto.</p>
                <div style={{ height: 350 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={validationData} innerRadius={100} outerRadius={140} paddingAngle={5} dataKey="value">
                        {validationData.map((entry, index) => {
                          const color = entry.name === 'Aprobadas' ? '#3DDC84' : entry.name === 'Rechazadas' ? '#DB4437' : '#F4B400';
                          return <Cell key={`cell-${index}`} fill={color} />;
                        })}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="bottom" height={36} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

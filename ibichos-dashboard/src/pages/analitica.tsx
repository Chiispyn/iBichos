import { useEffect, useState } from 'react';
import { db } from '../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, AreaChart, Area, LabelList
} from 'recharts';
import { Activity, Users, Camera, ShieldAlert, Clock, Smartphone, Bug, ShieldCheck, ChevronRight, MapPin, UserCircle2, Calendar, Brain, CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
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
  const [retentionStats, setRetentionStats] = useState({ day1: '0', day7: '0', day30: '0' });
  const [activationStats, setActivationStats] = useState({ activados: 0, abandono: 0 });  const [validationData, setValidationData] = useState<any[]>([]);
  const [monthlySessionsData, setMonthlySessionsData] = useState<any[]>([]);
  const [monthlyCapturesData, setMonthlyCapturesData] = useState<any[]>([]);
  const [genreData, setGenreData] = useState<any[]>([]);
  const [comunaData, setComunaData] = useState<any[]>([]);
  const [ageData, setAgeData] = useState<any[]>([]);
  
  // --- ESTADOS DE FILTRO REGIONAL ---
  const [rawUsers, setRawUsers] = useState<any[]>([]);
  const [regionsList, setRegionsList] = useState<string[]>([]);
  const [selectedRegion, setSelectedRegion] = useState('Todas');

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

  const traducirGenero = (g: string) => {
    const dict: Record<string, string> = { MALE: 'Masculino', FEMALE: 'Femenino', OTHER: 'Otro', PREFER_NOT_TO_SAY: 'Reservado', UNSPECIFIED: 'No definido' };
    return dict[g] || g;
  };

  const calcularEdad = (fechaNac: string) => {
    if (!fechaNac || fechaNac === 'N/A') return 0;
    try {
      // El formato en Android es DD/MM/YYYY
      const parts = fechaNac.split('/');
      if (parts.length !== 3) return 0;

      const day = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1; // Meses en JS son 0-11
      const year = parseInt(parts[2], 10);

      if (isNaN(day) || isNaN(month) || isNaN(year)) return 0;

      const birthDate = new Date(year, month, day);
      if (isNaN(birthDate.getTime())) return 0;

      const today = new Date();
      let age = today.getFullYear() - birthDate.getFullYear();
      const m = today.getMonth() - birthDate.getMonth();
      if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
      }
      return age;
    } catch {
      return 0;
    }
  };

  const clasificarEdad = (edad: number) => {
    if (edad <= 0 || isNaN(edad)) return 'N/A';
    if (edad < 18) return 'Menor de 18';
    if (edad <= 25) return '18-25 años';
    if (edad <= 35) return '26-35 años';
    if (edad <= 50) return '36-50 años';
    return 'Más de 50 años';
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // 1. PROCESAMIENTO DE USUARIOS Y LIGAS
        const userFirstCapture: Record<string, Date> = {};
        const userFirstSession: Record<string, Date> = {};
        const usersSnap = await getDocs(collection(db, 'users'));
        let totalUsers = 0;
        const levelCounts: Record<string, number> = {};
        const usersList: any[] = [];
        const regionsSet = new Set<string>();

        usersSnap.forEach(doc => {
          totalUsers++;
          const data = doc.data();
          const level = traducirNivel(data.gamification?.level || 'CASUAL');
          levelCounts[level] = (levelCounts[level] || 0) + 1;
          
          usersList.push(data);
          if (data.region) regionsSet.add(data.region);
        });

        setRawUsers(usersList);
        setRegionsList(Array.from(regionsSet).sort());

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

          if (data.userId && data.timestamp) {
            const capDate = data.timestamp.toDate ? data.timestamp.toDate() : new Date(data.timestamp);
            if (!userFirstCapture[data.userId] || capDate < userFirstCapture[data.userId]) {
              userFirstCapture[data.userId] = capDate;
            }
          };


          if (data.validationStatus === 'PENDING_REVIEW' || data.needsReview) pendingReview++;
          
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
        
        // Agrupación para cálculo de retención
        const userSessionsMap: Record<string, Date[]> = {};

        sessionsSnap.forEach(doc => {
          totalSessions++;
          const data = doc.data();
          const duration = data.durationMinutes || 0;
          totalMinutes += duration;


          // Guardar solo la PRIMERA sesión de cada usuario
          if (data.userId && data.startedAt) {
            const sessionDate = data.startedAt.toDate ? data.startedAt.toDate() : new Date(data.startedAt.toMillis());
            if (!userFirstSession[data.userId] || sessionDate < userFirstSession[data.userId]) {
              userFirstSession[data.userId] = sessionDate;
            }
          }

      
          if (data.startedAt) {
            // Manejo de compatibilidad para toDate() o toMillis()
            const dateObj = data.startedAt.toDate ? data.startedAt.toDate() : new Date(data.startedAt.toMillis());
            const dateStr = dateObj.toLocaleDateString('es-CL', { month: 'short', day: 'numeric' });
            const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
            const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);
            
            timelineData[dateStr] = (timelineData[dateStr] || 0) + duration;
            monthlySessionsMap[monthFormatted] = (monthlySessionsMap[monthFormatted] || 0) + duration;

            // Agrupar sesiones por usuario
            if (data.userId) {
              if (!userSessionsMap[data.userId]) userSessionsMap[data.userId] = [];
              userSessionsMap[data.userId].push(dateObj);
            }
          }
        });

        // Lógica matemática de Retención
        let eligibleD1 = 0, retainedD1 = 0;
        let eligibleD7 = 0, retainedD7 = 0;
        let eligibleD30 = 0, retainedD30 = 0;
        const hoy = new Date();

        Object.values(userSessionsMap).forEach(fechas => {
          fechas.sort((a, b) => a.getTime() - b.getTime());
          const primeraVez = fechas[0];
          const diasDesdeOrigen = Math.floor((hoy.getTime() - primeraVez.getTime()) / (1000 * 60 * 60 * 24));
          
          let maxDiaRetorno = 0;
          fechas.forEach(f => {
            const diff = Math.floor((f.getTime() - primeraVez.getTime()) / (1000 * 60 * 60 * 24));
            if (diff > maxDiaRetorno) maxDiaRetorno = diff;
          });

          if (diasDesdeOrigen >= 1) { eligibleD1++; if (maxDiaRetorno >= 1) retainedD1++; }
          if (diasDesdeOrigen >= 7) { eligibleD7++; if (maxDiaRetorno >= 7) retainedD7++; }
          if (diasDesdeOrigen >= 30) { eligibleD30++; if (maxDiaRetorno >= 30) retainedD30++; }
        });

        setRetentionStats({
          day1: eligibleD1 > 0 ? ((retainedD1 / eligibleD1) * 100).toFixed(1) : '0',
          day7: eligibleD7 > 0 ? ((retainedD7 / eligibleD7) * 100).toFixed(1) : '0',
          day30: eligibleD30 > 0 ? ((retainedD30 / eligibleD30) * 100).toFixed(1) : '0'
        });

        // CÁLCULO FINAL DE ACTIVACIÓN (< 10 MINUTOS)
        // -------------------------------------------------------------
        let activadosCount = 0;
        let abandonoCount = 0;

        // Recorremos todos los usuarios que han tenido al menos una sesión
        Object.keys(userFirstSession).forEach(uid => {
          const primeraSesion = userFirstSession[uid];
          const primeraCaptura = userFirstCapture[uid];

          if (primeraCaptura) {
            // Calculamos la diferencia en minutos
            const diffMinutos = (primeraCaptura.getTime() - primeraSesion.getTime()) / (1000 * 60);
            
            // Si tomó la foto entre 0 y 10 minutos después de su primera sesión
            if (diffMinutos <= 10 && diffMinutos >= 0) {
              activadosCount++; 
            } else {
              abandonoCount++; // Se demoró más de 10 min
            }
          } else {
            abandonoCount++; // Nunca ha tomado una foto, es un abandono del embudo
          }
        });

        const totalEvaluados = activadosCount + abandonoCount;
        setActivationStats({
          activados: totalEvaluados > 0 ? Math.round((activadosCount / totalEvaluados) * 100) : 0,
          abandono: totalEvaluados > 0 ? Math.round((abandonoCount / totalEvaluados) * 100) : 0
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

  // --- EFECTO PARA FILTRADO REGIONAL ---
  useEffect(() => {
    if (rawUsers.length === 0) return;

    let filtered = rawUsers;
    if (selectedRegion !== 'Todas') {
      filtered = rawUsers.filter(u => u.region === selectedRegion);
    }

    const genreCounts: Record<string, number> = {};
    const comunaCounts: Record<string, number> = {};
    const ageCounts: Record<string, number> = {
      'Menor de 18': 0, '18-25 años': 0, '26-35 años': 0, '36-50 años': 0, 'Más de 50 años': 0, 'N/A': 0
    };

    filtered.forEach(u => {
      // Género
      const gen = traducirGenero(u.gender || 'UNSPECIFIED');
      genreCounts[gen] = (genreCounts[gen] || 0) + 1;

      // Comuna
      const com = u.city || 'Sin comuna';
      comunaCounts[com] = (comunaCounts[com] || 0) + 1;

      // Edad
      if (u.birthDate && u.birthDate !== 'N/A') {
        const edad = calcularEdad(u.birthDate);
        const rango = clasificarEdad(edad);
        ageCounts[rango]++;
      } else {
        ageCounts['N/A']++;
      }
    });

    setGenreData(Object.entries(genreCounts).map(([name, value]) => ({ name, value })));
    setComunaData(Object.entries(comunaCounts).map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value).slice(0, 10)); // Top 10
    setAgeData(Object.entries(ageCounts).map(([name, value]) => ({ name, value }))
      .filter(item => item.value > 0));

  }, [selectedRegion, rawUsers]);

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
          <button className={`nav-link w-100 ${activeTab === 'demografia' ? 'active bg-success shadow' : 'text-success bg-white border'}`} onClick={() => setActiveTab('demografia')} style={{ fontWeight: 'bold' }}>
            <UserCircle2 size={18} className="me-2 mb-1" /> Demografía
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


          {/* Métrica: DE ACTIVACIÓN DE USUARIOS */}
          <div className="row g-4 mb-4">
            <div className="col-12">
              <div className="card ibichos-card p-4 shadow-sm border-0">
                <div className="mb-4">
                  <h4 className="fw-bold text-dark mb-1">Activación de Usuarios</h4>
                  <p className="text-muted small mb-0">Embudo de conversión: Registro → Primer Avistamiento</p>
                </div>

                <div className="row align-items-center g-4">
                  {/* Gráfico Donut de Activación */}
                  <div className="col-md-5 d-flex justify-content-center position-relative">
                    <div style={{ width: '220px', height: '220px', position: 'relative' }}>
                      <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                          <Pie
                            data={[
                              { name: 'Activados', value: activationStats.activados, fill: '#10B981' }, 
                              { name: 'Abandono', value: activationStats.abandono, fill: '#EF4444' }   
                            ]}
                            cx="50%"
                            cy="50%"
                            innerRadius={75}
                            outerRadius={105}
                            paddingAngle={3}
                            stroke="none"
                            dataKey="value"
                          />
                        </PieChart>
                      </ResponsiveContainer>
                      {/* Porcentaje Central Dinámico */}
                      <div className="position-absolute text-center w-100" style={{ top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }}>
                        <h2 className="fw-bolder mb-0" style={{ color: '#10B981', fontSize: '2.5rem', lineHeight: '1' }}>
                          {activationStats.activados}%
                        </h2>
                        <span className="text-muted fw-semibold" style={{ fontSize: '0.85rem' }}>Activados</span>
                      </div>
                    </div>
                  </div>

                  {/* Tarjetas Laterales */}
                  <div className="col-md-7">
                    <div className="row g-3 mb-3">
                      {/* Tarjeta Activación (Verde) */}
                      <div className="col-sm-6">
                        <div className="p-3 h-100" style={{ backgroundColor: '#F0FFF4', borderRadius: '12px' }}>
                          <div className="d-flex align-items-center mb-2">
                            <Activity size={16} className="text-success me-2" />
                            <span className="text-secondary small fw-semibold">Tasa de Activación</span>
                          </div>
                          <h2 className="fw-bold text-success mb-0">{activationStats.activados}%</h2>
                        </div>
                      </div>
                      {/* Tarjeta Abandono (Roja) */}
                      <div className="col-sm-6">
                        <div className="p-3 h-100" style={{ backgroundColor: '#FFF5F5', borderRadius: '12px' }}>
                          <div className="d-flex align-items-center mb-2">
                            <Activity size={16} className="text-danger me-2" style={{ transform: 'rotate(180deg)' }}/>
                            <span className="text-secondary small fw-semibold">Tasa de Abandono</span>
                          </div>
                          <h2 className="fw-bold text-danger mb-0">{activationStats.abandono}%</h2>
                        </div>
                      </div>
                    </div>

                    {/* Explicación Dinámica */}
                    <div className="p-3 rounded-3" style={{ backgroundColor: '#F8F9FA' }}>
                      <h6 className="text-secondary small fw-bold mb-2">Interpretación</h6>
                      <p className="text-muted small mb-0" style={{ lineHeight: '1.6' }}>
                        El <strong>{activationStats.activados}%</strong> de usuarios completan su primer avistamiento en los primeros <strong>10 minutos</strong> después de haber iniciado sesión por primera vez en la aplicación.
                      </p>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>

          {/* Métrica: RETENCIÓN DE USUARIOS */}
          <div className="row g-4 mb-4">
            <div className="col-12">
              <div className="card ibichos-card p-4 shadow-sm border-0">
                <div className="mb-4">
                  <h4 className="fw-bold text-dark mb-1">Retención de Usuarios</h4>
                  <p className="text-muted small mb-0">Porcentaje de exploradores que regresan a iBichos</p>
                </div>

                <div className="row g-3 mb-4 text-center">
                  <div className="col-md-4">
                    <div className="p-4" style={{ backgroundColor: '#F0FFF4', border: '2px solid #C6F6D5', borderRadius: '16px' }}>
                      <span className="text-muted small d-block mb-2 fw-bold">Día 1</span>
                      <h2 className="fw-bolder mb-3" style={{ color: '#2F855A', fontSize: '2.8rem' }}>{retentionStats.day1}%</h2>
                      <div className="progress mx-auto" style={{ height: '8px', width: '80%', backgroundColor: '#E2E8F0' }}>
                        <div className="progress-bar bg-success" style={{ width: `${retentionStats.day1}%` }}></div>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="p-4" style={{ backgroundColor: '#F0F9FF', border: '2px solid #BAE6FD', borderRadius: '16px' }}>
                      <span className="text-muted small d-block mb-2 fw-bold">Día 7</span>
                      <h2 className="fw-bolder mb-3" style={{ color: '#0284C7', fontSize: '2.8rem' }}>{retentionStats.day7}%</h2>
                      <div className="progress mx-auto" style={{ height: '8px', width: '80%', backgroundColor: '#E2E8F0' }}>
                        <div className="progress-bar bg-info" style={{ width: `${retentionStats.day7}%` }}></div>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="p-4" style={{ backgroundColor: '#FFFBEB', border: '2px solid #FEF3C7', borderRadius: '16px' }}>
                      <span className="text-muted small d-block mb-2 fw-bold">Día 30</span>
                      <h2 className="fw-bolder mb-3" style={{ color: '#D97706', fontSize: '2.8rem' }}>{retentionStats.day30}%</h2>
                      <div className="progress mx-auto" style={{ height: '8px', width: '80%', backgroundColor: '#E2E8F0' }}>
                        <div className="progress-bar bg-warning" style={{ width: `${retentionStats.day30}%` }}></div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="p-3 rounded-3" style={{ backgroundColor: '#F8F9FA', borderLeft: '4px solid #DEE2E6' }}>
                  <h6 className="text-secondary small fw-bold mb-2">Interpretación de Datos</h6>
                  <p className="text-muted small mb-0" style={{ lineHeight: '1.5' }}>
                    La retención mide la fidelidad de tu comunidad. Actualmente, el <strong>{retentionStats.day1}%</strong> de los usuarios vuelve tras su primer día, mientras que un <strong>{retentionStats.day30}%</strong> se mantiene activo tras un mes de uso.
                  </p>
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
          
          {/* Tarjeta Superior: Total de Capturas */}
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm border-primary border-opacity-25" style={{ cursor: 'pointer' }} onClick={() => navigate('/capturas')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-2">
                    <div className="bg-primary bg-opacity-10 p-3 rounded-circle me-3">
                      <Camera size={32} className="text-primary" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Inventario Biológico (Total de avistamientos)</h6>
                      <h3 className="fw-bold mb-0 text-primary">{stats.totalCaptures.toLocaleString('es-CL')}</h3>
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
            {/* Gráfico 1: Salud Pública (Doughnut con Porcentajes) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Riesgos para la Salud</h5>
                <p className="text-muted small mb-4">Distribución del nivel de peligrosidad de las especies</p>
                
                <div style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie 
                        data={dangerData} 
                        innerRadius={80} 
                        outerRadius={120} 
                        paddingAngle={4} 
                        dataKey="value"
                        labelLine={false}
                        // 1. Agregamos ": any" a los parámetros destructurados
                        label={({ cx, cy, midAngle, innerRadius, outerRadius, percent }: any) => {
                          // 2. Validación de seguridad rápida para que TS no se queje
                          if (midAngle === undefined || percent === undefined) return null;
                          
                          const RADIAN = Math.PI / 180;
                          const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
                          const x = cx + radius * Math.cos(-midAngle * RADIAN);
                          const y = cy + radius * Math.sin(-midAngle * RADIAN);
                          
                          if (percent < 0.04) return null; 
                          return (
                            <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontWeight="bold" fontSize={13}>
                              {`${(percent * 100).toFixed(1)}%`}
                            </text>
                          );
                        }}
                      >
                        {dangerData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={(DANGER_COLORS as any)[entry.name] || COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      {/* 3. Cambiamos (value: number) a (value: any) en el formatter */}
                      <Tooltip 
                        formatter={(value: any) => [`${value} avistamientos`, 'Cantidad']}
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                      />
                      <Legend verticalAlign="bottom" height={36} iconType="circle" wrapperStyle={{ fontSize: '13px', paddingTop: '20px' }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            {/* Gráfico 2: Categorías (Barras Multicolor con Leyendas) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Especies Identificadas</h5>
                <p className="text-muted small mb-4">Clasificación taxonómica de las capturas</p>
                
                <div style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    {/* Añadimos un margen derecho mayor (right: 40) para que el número final no se corte */}
                    <BarChart data={categoryData} layout="vertical" margin={{ top: 0, right: 40, left: 0, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.2} />
                      <XAxis type="number" hide /> {/* Ocultamos el eje X inferior para hacerlo más limpio */}
                      <YAxis dataKey="name" type="category" width={110} tick={{fontSize: 12, fill: '#475569', fontWeight: 500}} axisLine={false} tickLine={false} />
                      <Tooltip 
                        cursor={{fill: '#F1F5F9'}}
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                      />
                      <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={24}>
                        {/* Asignamos colores de la paleta a cada barra individualmente */}
                        {categoryData.map((_entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                        {/* Etiqueta con el número exacto al final de cada barra */}
                        <LabelList dataKey="value" position="right" fill="#64748B" fontSize={13} fontWeight="bold" />
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA DEMOGRAFÍA (Perfil de Usuarios) ---------------- */}
      {activeTab === 'demografia' && (
        <div className="tab-content fade-in-up">
          
          {/* Tarjeta de Filtro Regional */}
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm border-success border-opacity-25">
                <div className="card-body d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                  <div className="d-flex align-items-center">
                    <div className="bg-success bg-opacity-10 p-3 rounded-circle me-3">
                      <MapPin size={32} className="text-success" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Filtrar Análisis por Región</h6>
                      <h3 className="fw-bold mb-0 text-dark">{selectedRegion}</h3>
                    </div>
                  </div>
                  
                  {/* Selector de Región */}
                  <div className="flex-grow-1 max-w-md" style={{ maxWidth: '300px' }}>
                    <select 
                      className="form-select form-select-lg border-success shadow-sm fw-bold text-success"
                      value={selectedRegion}
                      onChange={(e) => setSelectedRegion(e.target.value)}
                    >
                      <option value="Todas">🇨🇱 Todas las regiones</option>
                      {regionsList.map(region => (
                        <option key={region} value={region}>{region}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            {/* Gráfico 1: Género (Pie Chart con Porcentajes) */}
            <div className="col-lg-4">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark"><UserCircle2 size={20} className="me-2 text-primary" /> Género</h5>
                <p className="text-muted small mb-4">Distribución en {selectedRegion}</p>
                
                <div style={{ height: 280 }}>
                  {genreData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie 
                          data={genreData} 
                          innerRadius={65} 
                          outerRadius={105} 
                          paddingAngle={4} 
                          dataKey="value"
                          labelLine={false}
                          label={({ cx, cy, midAngle, innerRadius, outerRadius, percent }: any) => {
                            if (midAngle === undefined || percent === undefined || percent < 0.05) return null;
                            const RADIAN = Math.PI / 180;
                            const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
                            const x = cx + radius * Math.cos(-midAngle * RADIAN);
                            const y = cy + radius * Math.sin(-midAngle * RADIAN);
                            return (
                              <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontWeight="bold" fontSize={13}>
                                {`${(percent * 100).toFixed(1)}%`}
                              </text>
                            );
                          }}
                        >
                          {genreData.map((_entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip 
                          formatter={(value: any) => [`${value} usuarios`, 'Cantidad']}
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                        />
                        <Legend verticalAlign="bottom" height={36} iconType="circle" wrapperStyle={{ fontSize: '13px', paddingTop: '15px' }} />
                      </PieChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="h-100 d-flex align-items-center justify-content-center text-muted fw-semibold bg-light rounded-3">Sin datos en esta zona</div>
                  )}
                </div>
              </div>
            </div>

            {/* Gráfico 2: Edades (Barras Verticales Multicolor) */}
            <div className="col-lg-4">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark"><Calendar size={20} className="me-2 text-warning" /> Edades</h5>
                <p className="text-muted small mb-4">Grupos etarios en {selectedRegion}</p>
                
                <div style={{ height: 280 }}>
                  {ageData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      {/* Damos un top margin para que los números no se corten arriba */}
                      <BarChart data={ageData} margin={{ top: 25, right: 0, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.2} />
                        <XAxis dataKey="name" tick={{fontSize: 11, fill: '#475569', fontWeight: 500}} axisLine={false} tickLine={false} interval={0} />
                        <YAxis hide /> {/* Ocultamos el eje Y porque ahora tenemos los números en las barras */}
                        <Tooltip 
                          cursor={{fill: '#F1F5F9'}} 
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }} 
                        />
                        <Bar dataKey="value" radius={[6, 6, 0, 0]} barSize={40}>
                          {ageData.map((_entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} /> // Usamos un offset (+2) para variar los colores respecto a otros gráficos
                          ))}
                          <LabelList dataKey="value" position="top" fill="#64748B" fontSize={13} fontWeight="bold" />
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="h-100 d-flex align-items-center justify-content-center text-muted fw-semibold bg-light rounded-3">Sin datos en esta zona</div>
                  )}
                </div>
              </div>
            </div>

            {/* Gráfico 3: Comunas (Barras Horizontales Multicolor) */}
            <div className="col-lg-4">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark"><MapPin size={20} className="me-2 text-danger" /> Top Comunas</h5>
                <p className="text-muted small mb-4">Concentración en {selectedRegion !== 'Todas' ? selectedRegion : 'todo el país'}</p>
                
                <div style={{ height: 280 }}>
                  {comunaData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      {/* Damos un right margin para que los números al final de la barra no se corten */}
                      <BarChart data={comunaData} layout="vertical" margin={{ top: 0, right: 35, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.2} />
                        <XAxis type="number" hide /> {/* Ocultamos eje X inferior */}
                        <YAxis dataKey="name" type="category" width={85} tick={{fontSize: 11, fill: '#475569', fontWeight: 500}} axisLine={false} tickLine={false} />
                        <Tooltip 
                          cursor={{fill: '#F1F5F9'}} 
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }} 
                        />
                        <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={20}>
                          {comunaData.map((_entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[(index + 4) % COLORS.length]} /> // Offset de color diferente
                          ))}
                          <LabelList dataKey="value" position="right" fill="#64748B" fontSize={12} fontWeight="bold" />
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="h-100 d-flex align-items-center justify-content-center text-muted fw-semibold bg-light rounded-3">Sin datos en esta zona</div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA MODERACIÓN (Eficacia IA) ---------------- */}
      {activeTab === 'moderacion' && (
        <div className="tab-content fade-in-up">
          
          {/* Tarjeta de Carga de Trabajo */}
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
              <div className="card ibichos-card p-4 shadow-sm border-0">
                
                {/* Cabecera */}
                <div className="d-flex align-items-center mb-4">
                  <div className="bg-primary bg-opacity-10 p-3 rounded-3 me-3">
                    <Brain size={28} className="text-primary" />
                  </div>
                  <div>
                    <h4 className="fw-bold mb-1 text-dark">Precisión del Motor de IA (Kindwise)</h4>
                    <p className="text-muted small mb-0">Rendimiento validado por usuarios reales</p>
                  </div>
                </div>

                {(() => {
                  const aprobadas = validationData.find(d => d.name === 'Aprobadas')?.value || 0;
                  const rechazadas = validationData.find(d => d.name === 'Rechazadas')?.value || 0;
                  const pendientes = validationData.find(d => d.name === 'Pendientes')?.value || 0;
                  const totalValidaciones = aprobadas + rechazadas + pendientes;
                  
                  const porc_aprobadas = totalValidaciones > 0 ? ((aprobadas / totalValidaciones) * 100).toFixed(1) : 0;
                  const porc_rechazadas = totalValidaciones > 0 ? ((rechazadas / totalValidaciones) * 100).toFixed(1) : 0;
                  const porc_pendientes = totalValidaciones > 0 ? ((pendientes / totalValidaciones) * 100).toFixed(1) : 0;

                  return (
                  <div className="row align-items-center g-4">
                      
                      {/* Gráfico Gauge - Ahora usa col-xl-5 para apilarse en pantallas < 1200px */}
                      <div className="col-xl-5 col-lg-12 d-flex flex-column align-items-center justify-content-center pt-2">
                        
                        <div style={{ width: '100%', maxWidth: '320px', height: '160px' }}>
                          <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                              <Pie
                                data={[
                                  { value: aprobadas, fill: '#3DDC84' },
                                  { value: pendientes, fill: '#F4B400' },
                                  { value: rechazadas, fill: '#DB4437' }
                                ]}
                                cx="50%"
                                cy="100%"
                                startAngle={180}
                                endAngle={0}
                                innerRadius={100}
                                outerRadius={145}
                                cornerRadius={6}
                                paddingAngle={2}
                                stroke="none"
                                dataKey="value"
                              />
                            </PieChart>
                          </ResponsiveContainer>
                        </div>
                        
                        <div className="text-center" style={{ marginTop: '-55px', zIndex: 10 }}>
                          <h1 className="fw-bolder mb-0 text-dark" style={{ fontSize: '3.8rem', lineHeight: '1', letterSpacing: '-2px' }}>
                            {porc_aprobadas}%
                          </h1>
                          <span className="text-secondary fw-bold text-uppercase d-block mt-1" style={{ fontSize: '0.85rem', letterSpacing: '2px' }}>
                            Éxito IA
                          </span>
                        </div>

                      </div>

                      {/* Cuadros de Información - Ahora usa col-xl-7 */}
                      <div className="col-xl-7 col-lg-12">
                        <div className="d-flex flex-column gap-3">
                          
                          {/* Cuadro VERDE */}
                          <div className="p-3 shadow-sm border border-success border-opacity-10" style={{ backgroundColor: '#F0FFF4', borderRadius: '12px' }}>
                            <div className="d-flex align-items-start gap-3">
                              <div className="bg-white rounded-circle p-2 shadow-sm border border-success border-opacity-25 flex-shrink-0">
                                <CheckCircle2 className="text-success" size={24} />
                              </div>
                              <div className="flex-grow-1 pt-1">
                                <h6 className="text-success fw-bold mb-1" style={{ fontSize: '1.05rem' }}>Aprobación Automática</h6>
                                <p className="text-muted small mb-2">{porc_aprobadas}% del total de validaciones</p>
                                <div className="progress" style={{ height: '8px', backgroundColor: '#C3E6CB', borderRadius: '4px' }}>
                                  <div className="progress-bar" style={{ backgroundColor: '#3DDC84', width: `${porc_aprobadas}%`, borderRadius: '4px' }}></div>
                                </div>
                              </div>
                            </div>
                          </div>

                          {/* Cuadro AMARILLO */}
                          <div className="p-3 shadow-sm border border-warning border-opacity-10" style={{ backgroundColor: '#FFFAF0', borderRadius: '12px' }}>
                            <div className="d-flex align-items-start gap-3">
                              <div className="bg-white rounded-circle p-2 shadow-sm border border-warning border-opacity-25 flex-shrink-0">
                                <AlertCircle className="text-warning" size={24} />
                              </div>
                              <div className="flex-grow-1 pt-1">
                                <h6 className="text-warning fw-bold mb-1" style={{ fontSize: '1.05rem' }}>Revisión Manual</h6>
                                <p className="text-muted small mb-2">{porc_pendientes}% del total de validaciones</p>
                                <div className="progress" style={{ height: '8px', backgroundColor: '#FFEeba', borderRadius: '4px' }}>
                                  <div className="progress-bar bg-warning" style={{ width: `${porc_pendientes}%`, borderRadius: '4px' }}></div>
                                </div>
                              </div>
                            </div>
                          </div>

                          {/* Cuadro ROJO */}
                          <div className="p-3 shadow-sm border border-danger border-opacity-10" style={{ backgroundColor: '#FFF5F5', borderRadius: '12px' }}>
                            <div className="d-flex align-items-start gap-3">
                              <div className="bg-white rounded-circle p-2 shadow-sm border border-danger border-opacity-25 flex-shrink-0">
                                <XCircle className="text-danger" size={24} />
                              </div>
                              <div className="flex-grow-1 pt-1">
                                <h6 className="text-danger fw-bold mb-1" style={{ fontSize: '1.05rem' }}>Identificaciones Rechazadas</h6>
                                <p className="text-muted small mb-2">{porc_rechazadas}% del total de validaciones</p>
                                <div className="progress" style={{ height: '8px', backgroundColor: '#F5C6CB', borderRadius: '4px' }}>
                                  <div className="progress-bar bg-danger" style={{ width: `${porc_rechazadas}%`, borderRadius: '4px' }}></div>
                                </div>
                              </div>
                            </div>
                          </div>

                        </div>
                      </div>
                    </div>
                  );
                })()}

              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

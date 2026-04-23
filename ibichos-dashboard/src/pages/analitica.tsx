import { useEffect, useState } from 'react';
import { db } from '../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line
} from 'recharts';
import { Activity, Users, Camera, ShieldAlert, Clock, Smartphone } from 'lucide-react';

const COLORS = ['#3DDC84', '#F4B400', '#DB4437', '#4285F4', '#9C27B0', '#00BCD4'];
const DANGER_COLORS = { 'Inofensivo': '#3DDC84', 'Precaución': '#F4B400', 'Venenoso': '#DB4437', 'Desconocido': '#9E9E9E' };

export default function Analitica() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ totalUsers: 0, totalCaptures: 0, pendingReview: 0, totalSessions: 0, totalMinutes: 0 });
  const [levelData, setLevelData] = useState<any[]>([]);
  const [categoryData, setCategoryData] = useState<any[]>([]);
  const [dangerData, setDangerData] = useState<any[]>([]);
  const [sessionsData, setSessionsData] = useState<any[]>([]);

  // Funciones de traducción
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
        // 1. Obtener Usuarios
        const usersSnap = await getDocs(collection(db, 'users'));
        let totalUsers = 0;
        const levelCounts: Record<string, number> = {};
        
        usersSnap.forEach(doc => {
          totalUsers++;
          const data = doc.data();
          const level = traducirNivel(data.gamification?.level || 'CASUAL');
          levelCounts[level] = (levelCounts[level] || 0) + 1;
        });

        // 2. Obtener Capturas
        const capturesSnap = await getDocs(collection(db, 'captures'));
        let totalCaptures = 0;
        let pendingReview = 0;
        const catCounts: Record<string, number> = {};
        const dangerCounts: Record<string, number> = {};

        capturesSnap.forEach(doc => {
          totalCaptures++;
          const data = doc.data();
          if (data.needsReview) pendingReview++;
          
          const cat = traducirCategoria(data.category || 'OTHER');
          catCounts[cat] = (catCounts[cat] || 0) + 1;

          const danger = traducirPeligro(data.dangerLevel || 'UNKNOWN');
          dangerCounts[danger] = (dangerCounts[danger] || 0) + 1;
        });

        // 3. Obtener Sesiones
        const sessionsSnap = await getDocs(collection(db, 'sessions'));
        let totalSessions = 0;
        let totalMinutes = 0;
        const timelineData: Record<string, number> = {};

        sessionsSnap.forEach(doc => {
          totalSessions++;
          const data = doc.data();
          const duration = data.durationMinutes || 0;
          totalMinutes += duration;
          
          if (data.startedAt) {
            // Extraer la fecha (ej: "23 abr")
            const dateObj = new Date(data.startedAt.toMillis());
            const dateStr = dateObj.toLocaleDateString('es-CL', { month: 'short', day: 'numeric' });
            
            // Agrupar los minutos jugados por día
            timelineData[dateStr] = (timelineData[dateStr] || 0) + duration;
          }
        });

        // Convertir el diccionario agrupado en un arreglo para Recharts
        const timeline = Object.entries(timelineData).map(([name, minutos]) => ({ name, minutos }));

        // Formatear para Recharts
        setStats({ totalUsers, totalCaptures, pendingReview, totalSessions, totalMinutes });
        
        setLevelData(Object.entries(levelCounts).map(([name, value]) => ({ name, value })));
        setCategoryData(Object.entries(catCounts).map(([name, value]) => ({ name, value })));
        setDangerData(Object.entries(dangerCounts).map(([name, value]) => ({ name, value })));
        setSessionsData(timeline);

      } catch (error) {
        console.error("Error fetching analytics:", error);
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
          <span className="visually-hidden">Cargando...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      <h2 className="mb-4 fw-bold text-success">
        <Activity className="me-2 mb-1" />
        Métricas de iBichos
      </h2>

      {/* KPI Cards */}
      <div className="row g-4 mb-5">
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body d-flex align-items-center">
              <div className="bg-success bg-opacity-10 p-3 rounded-circle me-3">
                <Users size={32} className="text-success" />
              </div>
              <div>
                <h6 className="text-muted mb-1">Exploradores Registrados</h6>
                <h3 className="fw-bold mb-0">{stats.totalUsers}</h3>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body d-flex align-items-center">
              <div className="bg-primary bg-opacity-10 p-3 rounded-circle me-3">
                <Camera size={32} className="text-primary" />
              </div>
              <div>
                <h6 className="text-muted mb-1">Capturas Totales</h6>
                <h3 className="fw-bold mb-0">{stats.totalCaptures}</h3>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body d-flex align-items-center">
              <div className="bg-warning bg-opacity-10 p-3 rounded-circle me-3">
                <ShieldAlert size={32} className="text-warning" />
              </div>
              <div>
                <h6 className="text-muted mb-1">Pendientes de Moderación</h6>
                <h3 className="fw-bold mb-0">{stats.pendingReview}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-4 mb-5">
        <div className="col-md-6">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body d-flex align-items-center">
              <div className="bg-info bg-opacity-10 p-3 rounded-circle me-3">
                <Smartphone size={32} className="text-info" />
              </div>
              <div>
                <h6 className="text-muted mb-1">Sesiones de App Registradas</h6>
                <h3 className="fw-bold mb-0">{stats.totalSessions}</h3>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body d-flex align-items-center">
              <div className="bg-danger bg-opacity-10 p-3 rounded-circle me-3">
                <Clock size={32} className="text-danger" />
              </div>
              <div>
                <h6 className="text-muted mb-1">Minutos Totales de Screen Time</h6>
                <h3 className="fw-bold mb-0">{stats.totalMinutes} <span className="fs-6 text-muted">minutos</span></h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Charts Row 1 */}
      <div className="row g-4 mb-4">
        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-4 h-100 p-3">
            <h5 className="fw-bold mb-4">Distribución de Ligas (Usuarios)</h5>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={levelData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.3} />
                  <XAxis dataKey="name" tick={{fontSize: 12}} />
                  <YAxis allowDecimals={false} />
                  <Tooltip cursor={{fill: 'rgba(61, 220, 132, 0.1)'}} />
                  <Bar dataKey="value" fill="#3DDC84" radius={[4, 4, 0, 0]} name="Usuarios" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-4 h-100 p-3">
            <h5 className="fw-bold mb-4">Peligrosidad de Especies</h5>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={dangerData}
                    innerRadius={80}
                    outerRadius={110}
                    paddingAngle={5}
                    dataKey="value"
                  >
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
      </div>

      {/* Charts Row 2 */}
      <div className="row g-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm rounded-4 p-3">
            <h5 className="fw-bold mb-4">Especies Identificadas por Categoría</h5>
            <div style={{ height: 350 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={categoryData} layout="vertical" margin={{ top: 5, right: 30, left: 50, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.3} />
                  <XAxis type="number" allowDecimals={false} />
                  <YAxis dataKey="name" type="category" width={100} tick={{fontSize: 12}} />
                  <Tooltip cursor={{fill: 'rgba(61, 220, 132, 0.1)'}} />
                  <Bar dataKey="value" fill="#4285F4" radius={[0, 4, 4, 0]} name="Capturas">
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>

      {/* Charts Row 3: Sessions */}
      <div className="row g-4 mt-1">
        <div className="col-12">
          <div className="card border-0 shadow-sm rounded-4 p-3">
            <h5 className="fw-bold mb-4">Retención Diaria: Minutos de Uso de la App</h5>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={sessionsData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.3} />
                  <XAxis dataKey="name" tick={{fontSize: 12}} />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="minutos" stroke="#9C27B0" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 8 }} name="Minutos Jugados" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

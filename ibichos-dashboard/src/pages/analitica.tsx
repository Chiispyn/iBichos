import { useEffect, useState } from 'react';
import { db } from '../config/firebaseConfig';
import { collection, getDocs, doc, setDoc } from 'firebase/firestore';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, AreaChart, Area, LabelList
} from 'recharts';
import { Activity, Users, Camera, ShieldAlert, Clock, Smartphone, Bug, ShieldCheck, ChevronRight, MapPin, UserCircle2, Calendar, Brain, CheckCircle2, XCircle, AlertCircle, Download, Database } from 'lucide-react';
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
  const [activationStats, setActivationStats] = useState({ activados: 0, abandono: 0 }); 
  const [validationData, setValidationData] = useState<any[]>([]);
  const [shadowbanData, setShadowbanData] = useState<any[]>([]);
  const [aiConfidenceData, setAiConfidenceData] = useState<any[]>([]);
  const [medalsData, setMedalsData] = useState<any[]>([]);
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
    const dict: Record<string, string> = { HYMENOPTERA: 'Abejas/Avispas', ARACHNIDA: 'Arácnidos', ARACHNID: 'Arácnidos', COLEOPTERA: 'Escarabajos', LEPIDOPTERA: 'Mariposas', DIPTERA: 'Moscas', BLATTODEA: 'Cucarachas', HEMIPTERA: 'Chinches', ORTHOPTERA: 'Grillos/Saltamontes', ODONATA: 'Libélulas', OTHER: 'Otro' };
    return dict[cat] || cat;
  };

  const traducirMedalla = (medalla: string) => {
    const dict: Record<string, string> = { 
      FIRST_CAPTURE: 'Primer Avistamiento',
      NOVICE_RESEARCHER: 'Investigador Novato',
      BRAVE_HUNTER: 'Cazador Valiente',
      ARACHNOLOGIST: 'Aracnólogo',
      LEPIDOPTEROLOGIST: 'Lepidopterólogo',
      POLLINATOR_FRIEND: 'Amigo Polinizador',
      COLEOPTEROLOGIST: 'Coleopterólogo'
    };
    return dict[medalla] || medalla.replace(/_/g, ' ');
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
        let healthyUsers = 0;
        let shadowbannedUsers = 0;
        const levelCounts: Record<string, number> = {};
        const medalsCounts: Record<string, number> = {};
        const usersList: any[] = [];
        const regionsSet = new Set<string>();

        usersSnap.forEach(doc => {
          totalUsers++;
          const data = doc.data();
          const level = traducirNivel(data.gamification?.level || 'CASUAL');
          levelCounts[level] = (levelCounts[level] || 0) + 1;

          // Conteo de Medallas
          const medalsList = data.gamification?.medals || [];
          if (Array.isArray(medalsList)) {
            medalsList.forEach(m => {
              const nombreMedalla = traducirMedalla(m);
              medalsCounts[nombreMedalla] = (medalsCounts[nombreMedalla] || 0) + 1;
            });
          }

          usersList.push(data);
          if (data.region) regionsSet.add(data.region);
          if (data.isShadowBanned) shadowbannedUsers++;
          else healthyUsers++;
        });

        setRawUsers(usersList);
        setRegionsList(Array.from(regionsSet).sort());

        // 2. PROCESAMIENTO DE CAPTURAS E INTELIGENCIA ARTIFICIAL
        const capturesSnap = await getDocs(collection(db, 'captures'));
        let totalCaptures = 0;
        let pendingReview = 0;
        const catCounts: Record<string, number> = {};
        const dangerCounts: Record<string, number> = {};
        const validationCounts: Record<string, number> = { 'Aprobadas': 0, 'Rechazadas (IA)': 0, 'Rechazadas (Admin)': 0, 'Pendientes': 0 };
        const monthlyCapturesMap: Record<string, number> = {};
        const aiConfidenceMap: Record<string, { totalProb: number, count: number }> = {};

        capturesSnap.forEach(doc => {
          totalCaptures++;
          const data = doc.data();

          if (data.userId && data.timestamp) {
            const capDate = data.timestamp.toDate ? data.timestamp.toDate() : new Date(data.timestamp);
            if (!userFirstCapture[data.userId] || capDate < userFirstCapture[data.userId]) {
              userFirstCapture[data.userId] = capDate;
            }
          };

          const currentStatus = data.status || data.validationStatus;
          if (currentStatus === 'PENDING_REVIEW' || data.needsReview) pendingReview++;

          // Agrupación por categoría (Biodiversidad)
          const cat = traducirCategoria(data.category || 'OTHER');
          catCounts[cat] = (catCounts[cat] || 0) + 1;

          // Agrupación por riesgo (Salud Pública)
          const danger = traducirPeligro(data.dangerLevel || 'UNKNOWN');
          dangerCounts[danger] = (dangerCounts[danger] || 0) + 1;

          // Registro de confianza de la IA
          if (data.probability !== undefined) {
            if (!aiConfidenceMap[cat]) aiConfidenceMap[cat] = { totalProb: 0, count: 0 };
            aiConfidenceMap[cat].totalProb += data.probability;
            aiConfidenceMap[cat].count += 1;
          }

          // Cálculo de Eficacia IA: Separando rechazos de la IA vs Humanos
          if (currentStatus === 'REJECTED' || currentStatus === 'DELETED') {
            if (data.moderatedBy) {
              validationCounts['Rechazadas (Admin)']++;
            } else {
              validationCounts['Rechazadas (IA)']++;
            }
          } else if ((data.probability || 0) < 0.40 && !data.moderatedBy && currentStatus !== 'APPROVED') {
            // Caso borde: fue subida, tiene prob < 40 pero sin status guardado aún
            validationCounts['Rechazadas (IA)']++;
          } else if (currentStatus === 'PENDING_REVIEW' || data.needsReview) {
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
        const monthlySessionsMap: Record<string, number> = {};

        // Agrupación para cálculo de uso diario (con ordenamiento cronológico)
        const timelineMap: Record<string, { name: string, minutos: number, dateObj: Date }> = {};
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
            // Usamos YYYY-MM-DD como clave para agrupar y ordenar fácilmente
            const dateKey = dateObj.toISOString().split('T')[0];
            const dateStr = dateObj.toLocaleDateString('es-CL', { month: 'short', day: 'numeric' });
            const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
            const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);

            if (!timelineMap[dateKey]) {
              timelineMap[dateKey] = { name: dateStr, minutos: 0, dateObj: dateObj };
            }
            timelineMap[dateKey].minutos += duration;
            monthlySessionsMap[monthFormatted] = (monthlySessionsMap[monthFormatted] || 0) + duration;

            // Agrupar sesiones por usuario
            if (data.userId) {
              if (!userSessionsMap[data.userId]) userSessionsMap[data.userId] = [];
              userSessionsMap[data.userId].push(dateObj);
            }
          }
        });

        // Convertimos el mapa en array y ORDENAMOS por fecha real
        const sortedSessionsData = Object.values(timelineMap)
          .sort((a, b) => a.dateObj.getTime() - b.dateObj.getTime())
          .map(item => ({ name: item.name, minutos: item.minutos }));

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
        const activadosPorc = totalEvaluados > 0 ? Math.round((activadosCount / totalEvaluados) * 100) : 0;
        setActivationStats({
          activados: activadosPorc,
          abandono: totalEvaluados > 0 ? 100 - activadosPorc : 0
        });

        // --- ORDENAMIENTO LÓGICO Y ALFABÉTICO ---
        
        const levelOrder = ['Casual', 'Amateur', 'Explorador', 'Entomólogo', 'Maestro de Bichos'];
        const levelArray = Object.entries(levelCounts).map(([name, value]) => ({ name, value }));
        levelArray.sort((a, b) => {
          const idxA = levelOrder.indexOf(a.name);
          const idxB = levelOrder.indexOf(b.name);
          return (idxA === -1 ? 99 : idxA) - (idxB === -1 ? 99 : idxB);
        });

        const categoryArray = Object.entries(catCounts).map(([name, value]) => ({ name, value }));
        categoryArray.sort((a, b) => {
          if (a.name === 'Otro') return 1;
          if (b.name === 'Otro') return -1;
          return a.name.localeCompare(b.name);
        });

        const dangerOrder = ['Inofensivo', 'Precaución', 'Venenoso', 'Desconocido'];
        const dangerArray = Object.entries(dangerCounts).map(([name, value]) => ({ name, value }));
        dangerArray.sort((a, b) => {
          const idxA = dangerOrder.indexOf(a.name);
          const idxB = dangerOrder.indexOf(b.name);
          return (idxA === -1 ? 99 : idxA) - (idxB === -1 ? 99 : idxB);
        });

        const aiArray = Object.entries(aiConfidenceMap).map(([name, stats]) => ({
          name,
          value: Math.round((stats.totalProb / stats.count) * 100)
        }));
        aiArray.sort((a, b) => {
          if (a.name === 'Otro') return 1;
          if (b.name === 'Otro') return -1;
          return a.name.localeCompare(b.name);
        });

        // --- ASIGNACIÓN DE ESTADOS ---

        setStats({ totalUsers, totalCaptures, pendingReview, totalSessions, totalMinutes });
        setLevelData(levelArray);
        setCategoryData(categoryArray);
        setDangerData(dangerArray);
        setValidationData(Object.entries(validationCounts).map(([name, value]) => ({ name, value })));
        setShadowbanData([
          { name: 'Sanos', value: healthyUsers },
          { name: 'Penalizados', value: shadowbannedUsers }
        ]);
        setAiConfidenceData(aiArray);
        setMedalsData(Object.entries(medalsCounts)
          .map(([name, value]) => ({ name, value }))
          .sort((a, b) => b.value - a.value)
          .slice(0, 5) // Top 5 medallas
        );
        setSessionsData(sortedSessionsData);
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

    const genreArray = Object.entries(genreCounts).map(([name, value]) => ({ name, value }));
    genreArray.sort((a, b) => {
      // Dejar 'No definido' o 'Reservado' al final, el resto alfabético
      if (a.name.includes('definido') || a.name.includes('Reservado') || a.name === 'Otro') return 1;
      if (b.name.includes('definido') || b.name.includes('Reservado') || b.name === 'Otro') return -1;
      return a.name.localeCompare(b.name);
    });

    setGenreData(genreArray);
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

  // --- FUNCIÓN PARA EXPORTAR REPORTE ---
  const handleExportCSV = () => {
    // Definimos el contenido del CSV
    let csvContent = "data:text/csv;charset=utf-8,\n";
    csvContent += "=== REPORTE DE INTELIGENCIA IBICHOS ===\n\n";
    
    // 1. Métricas Globales
    csvContent += "METRICAS GLOBALES\n";
    csvContent += `Usuarios Totales,${stats.totalUsers}\n`;
    csvContent += `Capturas Totales,${stats.totalCaptures}\n`;
    csvContent += `Capturas Pendientes de Revision,${stats.pendingReview}\n`;
    csvContent += `Minutos de Uso Total,${stats.totalMinutes}\n`;
    csvContent += `Sesiones Totales,${stats.totalSessions}\n\n`;

    // 2. Retención y Activación
    csvContent += "RETENCION Y ACTIVACION\n";
    csvContent += `Tasa Activacion (Primeros 10 min),${activationStats.activados}%\n`;
    csvContent += `Retencion Dia 1,${retentionStats.day1}%\n`;
    csvContent += `Retencion Dia 7,${retentionStats.day7}%\n`;
    csvContent += `Retencion Dia 30,${retentionStats.day30}%\n\n`;

    // 3. Eficacia IA
    csvContent += "EFICACIA INTELIGENCIA ARTIFICIAL\n";
    csvContent += "Estado,Cantidad\n";
    validationData.forEach(d => {
      csvContent += `${d.name},${d.value}\n`;
    });
    csvContent += "\n";

    // 4. Biodiversidad (Categorías)
    csvContent += "BIODIVERSIDAD (ESPECIES IDENTIFICADAS)\n";
    csvContent += "Categoria,Cantidad\n";
    categoryData.forEach(d => {
      csvContent += `${d.name},${d.value}\n`;
    });
    csvContent += "\n";

    // 5. Salud Pública (Nivel Peligro)
    csvContent += "SALUD PUBLICA (NIVEL DE RIESGO)\n";
    csvContent += "Nivel,Cantidad\n";
    dangerData.forEach(d => {
      csvContent += `${d.name},${d.value}\n`;
    });
    csvContent += "\n";

    // 6. Demografía (Filtro Actual)
    csvContent += `DEMOGRAFIA (Filtro: ${selectedRegion})\n`;
    csvContent += "Rango Etario,Cantidad\n";
    ageData.forEach(d => {
      csvContent += `${d.name},${d.value}\n`;
    });

    // Crear y descargar el archivo
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    const dateStr = new Date().toLocaleDateString('es-CL').replace(/\//g, '-');
    link.setAttribute("download", `Reporte_iBichos_${dateStr}.csv`);
    document.body.appendChild(link); // Requerido para Firefox
    link.click();
    document.body.removeChild(link);
  };

  // --- FUNCIÓN PARA GUARDAR SNAPSHOT EN FIRESTORE ---
  const handleSaveSnapshot = async () => {
    try {
      const now = new Date();
      // Formato YYYY-MM para que haya un documento único por mes
      const monthStr = now.toISOString().slice(0, 7); 
      
      const snapshotData = {
        fechaGuardado: now.toISOString(),
        mes: monthStr,
        metricasGlobales: stats,
        retencion: retentionStats,
        activacion: activationStats,
        eficaciaIA: validationData,
        biodiversidad: categoryData,
        saludPublica: dangerData,
        comunidadSana: shadowbanData
      };

      // Guardamos en una nueva colección llamada 'reportes_historicos'
      // Usamos el mes (ej. "2026-04") como ID del documento para que se actualice si le dan click varias veces en el mismo mes
      await setDoc(doc(db, 'reportes_historicos', monthStr), snapshotData);
      
      alert(`✅ ¡Éxito! La instantánea estadística de ${monthStr} se ha guardado en la nube (Firestore).`);
    } catch (error) {
      console.error("Error al guardar la instantánea en Firestore: ", error);
      alert("❌ Hubo un error al guardar el reporte en la nube.");
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <h2 className="mb-0 fw-bold text-success">
          <Activity className="me-2 mb-1" />
          Panel de Inteligencia iBichos
        </h2>
        <div className="d-flex gap-2 flex-wrap">
          <button 
            onClick={handleSaveSnapshot}
            className="btn btn-success d-flex align-items-center fw-bold shadow-sm"
            disabled={loading}
            title="Guardar un registro histórico en la base de datos"
          >
            <Database size={18} className="me-2" />
            Guardar en la Nube
          </button>
          <button 
            onClick={handleExportCSV}
            className="btn btn-outline-success d-flex align-items-center fw-bold shadow-sm"
            disabled={loading}
            title="Descargar datos en formato Excel/CSV"
          >
            <Download size={18} className="me-2" />
            Exportar CSV
          </button>
        </div>
      </div>

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
                  {/* Gráfico de Barras de Activación */}
                  <div className="col-md-5 d-flex justify-content-center">
                    <div style={{ width: '100%', height: '200px' }}>
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart
                          data={[
                            { name: 'Activados', value: activationStats.activados, fill: '#10B981' },
                            { name: 'Abandono', value: activationStats.abandono, fill: '#EF4437' }
                          ]}
                          layout="vertical"
                          margin={{ top: 20, right: 50, left: 20, bottom: 0 }}
                        >
                          <XAxis type="number" hide />
                          <YAxis dataKey="name" type="category" hide />
                          <Tooltip cursor={{ fill: 'transparent' }} />
                          <Bar dataKey="value" radius={[0, 10, 10, 0]} barSize={40}>
                            <LabelList dataKey="value" position="right" formatter={(v: any) => `${v}%`} fill="#334155" fontWeight="bold" />
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
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
                            <Activity size={16} className="text-danger me-2" style={{ transform: 'rotate(180deg)' }} />
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
                      <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                      <YAxis allowDecimals={false} />
                      <Tooltip cursor={{ fill: 'rgba(61, 220, 132, 0.1)' }} />
                      <Bar dataKey="value" fill="#3DDC84" radius={[4, 4, 0, 0]} name="Exploradores" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
            {/* Gráfico: Uso Diario (Área Lineal) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-3 shadow-sm border-0">
                <h5 className="fw-bold mb-4 text-dark">Uso Diario (Minutos)</h5>
                <div style={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={sessionsData}>
                      <defs>
                        <linearGradient id="colorMin" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#9C27B0" stopOpacity={0.3} />
                          <stop offset="95%" stopColor="#9C27B0" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.1} />
                      <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
                      <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
                      <Tooltip
                        contentStyle={{ borderRadius: '10px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                      />
                      <Area
                        type="linear"
                        dataKey="minutos"
                        stroke="#9C27B0"
                        strokeWidth={3}
                        fillOpacity={1}
                        fill="url(#colorMin)"
                        activeDot={{ r: 6, strokeWidth: 0 }}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>

          {/* NUEVO GRÁFICO: Top Medallas */}
          <div className="row g-4 mt-1">
            <div className="col-lg-12">
              <div className="card ibichos-card p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Logros Desbloqueados por la Comunidad</h5>
                <p className="text-muted small mb-4">Top 5 medallas más obtenidas según historial de Gamificación</p>
                <div style={{ height: 280 }}>
                  {medalsData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={medalsData} layout="vertical" margin={{ top: 0, right: 40, left: 20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.2} />
                        <XAxis type="number" hide />
                        <YAxis dataKey="name" type="category" width={150} tick={{ fontSize: 11, fill: '#475569', fontWeight: 600 }} axisLine={false} tickLine={false} />
                        <Tooltip
                          cursor={{ fill: '#F1F5F9' }}
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                          formatter={(value) => [`${value} usuarios`, 'Obtenida por']}
                        />
                        <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={25}>
                          {medalsData.map((_entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                          <LabelList dataKey="value" position="right" fill="#64748B" fontSize={13} fontWeight="bold" />
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                     <div className="h-100 d-flex align-items-center justify-content-center text-muted fw-semibold bg-light rounded-3">
                       No hay medallas registradas aún
                     </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA BIODIVERSIDAD (Ecosistema) ---------------- */}
      {activeTab === 'biodiversidad' && (
        <div className="tab-content fade-in-up">

          {/* Tarjeta Superior: Inventario Biológico (Lleva al Catálogo) */}
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm border-primary border-opacity-25" style={{ cursor: 'pointer' }} onClick={() => navigate('/catalogo')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-2">
                    <div className="bg-primary bg-opacity-10 p-3 rounded-circle me-3">
                      <Camera size={32} className="text-primary" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Inventario Biológico (Especies Verificadas)</h6>
                      <h3 className="fw-bold mb-0 text-primary">{stats.totalCaptures.toLocaleString('es-CL')}</h3>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mt-2 border-top pt-2">
                    <small className="text-primary fw-bold">Ver Catálogo Completo</small>
                    <ChevronRight size={14} className="text-primary" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            {/* Gráfico 1: Salud Pública (BARRAS HORIZONTALES) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Riesgos para la Salud</h5>
                <p className="text-muted small mb-4">Distribución del nivel de peligrosidad de las especies</p>

                <div style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={dangerData} layout="vertical" margin={{ top: 0, right: 60, left: 20, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.2} />
                      <XAxis type="number" hide />
                      <YAxis dataKey="name" type="category" width={100} tick={{ fontSize: 12, fill: '#475569', fontWeight: 600 }} axisLine={false} tickLine={false} />
                      <Tooltip
                        cursor={{ fill: '#F1F5F9' }}
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                      />
                      <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={30}>
                        {dangerData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={(DANGER_COLORS as any)[entry.name] || COLORS[index % COLORS.length]} />
                        ))}
                        <LabelList dataKey="value" position="right" fill="#64748B" fontSize={13} fontWeight="bold" />
                      </Bar>
                    </BarChart>
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
                      <YAxis dataKey="name" type="category" width={110} tick={{ fontSize: 12, fill: '#475569', fontWeight: 500 }} axisLine={false} tickLine={false} />
                      <Tooltip
                        cursor={{ fill: '#F1F5F9' }}
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
            {/* Gráfico 1: Género (BARRAS HORIZONTALES) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark"><UserCircle2 size={20} className="me-2 text-primary" /> Género</h5>
                <p className="text-muted small mb-4">Distribución en {selectedRegion}</p>

                <div style={{ height: 280 }}>
                  {genreData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={genreData} layout="vertical" margin={{ top: 0, right: 60, left: 20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.2} />
                        <XAxis type="number" hide />
                        <YAxis dataKey="name" type="category" width={100} tick={{ fontSize: 12, fill: '#475569', fontWeight: 600 }} axisLine={false} tickLine={false} />
                        <Tooltip
                          cursor={{ fill: '#F1F5F9' }}
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                        />
                        <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={25}>
                          {genreData.map((_entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                          <LabelList dataKey="value" position="right" fill="#64748B" fontSize={13} fontWeight="bold" />
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="h-100 d-flex align-items-center justify-content-center text-muted fw-semibold bg-light rounded-3">Sin datos en esta zona</div>
                  )}
                </div>
              </div>
            </div>

            {/* Gráfico 2: Edades (Barras Verticales Multicolor) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark"><Calendar size={20} className="me-2 text-warning" /> Edades</h5>
                <p className="text-muted small mb-4">Grupos etarios en {selectedRegion}</p>

                <div style={{ height: 280 }}>
                  {ageData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      {/* Damos un top margin para que los números no se corten arriba */}
                      <BarChart data={ageData} margin={{ top: 25, right: 0, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.2} />
                        <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#475569', fontWeight: 500 }} axisLine={false} tickLine={false} interval={0} />
                        <YAxis hide /> {/* Ocultamos el eje Y porque ahora tenemos los números en las barras */}
                        <Tooltip
                          cursor={{ fill: '#F1F5F9' }}
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

          </div>

          {/* BOTÓN A GEOGRAFÍA */}
          <div className="mt-4 text-center">
            <button className="btn btn-success px-5 py-3 fw-bold shadow-sm" onClick={() => navigate('/geografia')}>
              Ver Detalle Geográfico Completo <ChevronRight size={20} className="ms-2" />
            </button>
          </div>
        </div>
      )}

      {/* ---------------- PESTAÑA MODERACIÓN (Eficacia IA) ---------------- */}
      {activeTab === 'moderacion' && (
        <div className="tab-content fade-in-up">

          {/* Tarjeta de Carga de Trabajo (Lleva a Moderación) */}
          <div className="row g-4 mb-4">
            <div className="col-md-12">
              <div className="card ibichos-card shadow-sm border-warning" style={{ cursor: 'pointer' }} onClick={() => navigate('/capturas')}>
                <div className="card-body">
                  <div className="d-flex align-items-center mb-2">
                    <div className="bg-warning bg-opacity-10 p-3 rounded-circle me-3">
                      <ShieldAlert size={32} className="text-warning" />
                    </div>
                    <div>
                      <h6 className="text-muted mb-1">Bandeja de Moderación (Pendientes)</h6>
                      <h3 className="fw-bold mb-0 text-warning">{stats.pendingReview}</h3>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mt-2 border-top pt-2">
                    <small className="text-warning fw-bold">Gestionar Pendientes y Rechazos</small>
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
                  const rechazadasIA = validationData.find(d => d.name === 'Rechazadas (IA)')?.value || 0;
                  const rechazadasAdmin = validationData.find(d => d.name === 'Rechazadas (Admin)')?.value || 0;
                  const rechazadasTotal = rechazadasIA + rechazadasAdmin;
                  const pendientes = validationData.find(d => d.name === 'Pendientes')?.value || 0;
                  const totalValidaciones = aprobadas + rechazadasTotal + pendientes;

                  const porc_aprobadas = totalValidaciones > 0 ? ((aprobadas / totalValidaciones) * 100).toFixed(1) : 0;
                  const porc_rechazadas = totalValidaciones > 0 ? ((rechazadasTotal / totalValidaciones) * 100).toFixed(1) : 0;
                  const porc_pendientes = totalValidaciones > 0 ? ((pendientes / totalValidaciones) * 100).toFixed(1) : 0;

                  return (
                    <div className="row align-items-center g-4">

                      {/* Gráfico de Barras de Precisión IA */}
                      <div className="col-xl-5 col-lg-12 pt-2">
                        <div style={{ width: '100%', height: '200px' }}>
                          <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                              data={[
                                { name: 'Aprobadas (IA)', value: aprobadas, fill: '#3DDC84' },
                                { name: 'Revisión Manual', value: pendientes, fill: '#F4B400' },
                                { name: 'Rechazadas (IA)', value: rechazadasIA, fill: '#DB4437' },
                                { name: 'Borradas por Admin', value: rechazadasAdmin, fill: '#64748B' }
                              ]}
                              layout="vertical"
                              margin={{ top: 10, right: 60, left: 20, bottom: 10 }}
                            >
                              <XAxis type="number" hide />
                              <YAxis dataKey="name" type="category" width={80} tick={{ fontSize: 12, fontWeight: 600 }} axisLine={false} tickLine={false} />
                              <Tooltip cursor={{ fill: 'transparent' }} />
                              <Bar dataKey="value" radius={[0, 8, 8, 0]} barSize={35}>
                                <LabelList dataKey="value" position="right" fill="#334155" fontWeight="bold" />
                              </Bar>
                            </BarChart>
                          </ResponsiveContainer>
                        </div>
                        <div className="text-center mt-3">
                          <h2 className="fw-bolder mb-0 text-success" style={{ fontSize: '2.5rem' }}>
                            {porc_aprobadas}%
                          </h2>
                          <span className="text-secondary fw-bold text-uppercase" style={{ fontSize: '0.8rem', letterSpacing: '1px' }}>
                            Precisión Global
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

          {/* NUEVOS GRÁFICOS: SHADOWBAN Y CONFIANZA IA */}
          <div className="row g-4 mt-1">
            {/* Gráfico de Shadowban (PieChart) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Salud de la Comunidad</h5>
                <p className="text-muted small mb-4">Usuarios activos vs penalizados (Shadowban)</p>
                <div style={{ height: 250 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={shadowbanData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={90}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        <Cell fill="#3DDC84" /> {/* Sanos */}
                        <Cell fill="#DB4437" /> {/* Penalizados */}
                      </Pie>
                      <Tooltip 
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                        formatter={(value) => [`${value} usuarios`, 'Cantidad']}
                      />
                      <Legend verticalAlign="bottom" height={36} iconType="circle" />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            {/* Gráfico de Confianza IA (BarChart) */}
            <div className="col-lg-6">
              <div className="card ibichos-card h-100 p-4 shadow-sm border-0">
                <h5 className="fw-bold mb-1 text-dark">Eficacia de IA por Especie</h5>
                <p className="text-muted small mb-4">Porcentaje de confianza promedio del modelo</p>
                <div style={{ height: 250 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={aiConfidenceData} margin={{ top: 20, right: 0, left: -20, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.2} />
                      <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#475569' }} axisLine={false} tickLine={false} />
                      <YAxis tick={{ fontSize: 11, fill: '#475569' }} axisLine={false} tickLine={false} domain={[0, 100]} />
                      <Tooltip 
                        cursor={{ fill: '#F1F5F9' }}
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                        formatter={(value) => [`${value}%`, 'Confianza Promedio']}
                      />
                      <Bar dataKey="value" radius={[4, 4, 0, 0]} barSize={30}>
                        {aiConfidenceData.map((_entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Bar>
                    </BarChart>
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

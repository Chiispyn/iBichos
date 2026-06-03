// src/pages/Analitica/useAnalitica.ts
import { useState, useEffect } from 'react';
import { db } from '../../config/firebaseConfig';
import { collection, getDocs, doc, setDoc, getDoc } from 'firebase/firestore';

// --- FUNCIONES DE APOYO (Fuera del hook para mejor rendimiento) ---
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
    FIRST_CAPTURE: 'Primer Avistamiento', NOVICE_RESEARCHER: 'Investigador Novato', BRAVE_HUNTER: 'Cazador Valiente', ARACHNOLOGIST: 'Aracnólogo', LEPIDOPTEROLOGIST: 'Lepidopterólogo', POLLINATOR_FRIEND: 'Amigo Polinizador', COLEOPTEROLOGIST: 'Coleopterólogo'
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
    const parts = fechaNac.split('/');
    if (parts.length !== 3) return 0;
    const day = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
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

export function useAnalitica() {
  const [activeTab, setActiveTab] = useState('actividad');
  const [loading, setLoading] = useState(true);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  // Estados
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
  
  const [rawUsers, setRawUsers] = useState<any[]>([]);
  const [regionsList, setRegionsList] = useState<string[]>([]);
  const [selectedRegion, setSelectedRegion] = useState('Todas');

  // 1. EFECTO PRINCIPAL DE CARGA DE DATOS
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

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

          const cat = traducirCategoria(data.category || 'OTHER');
          catCounts[cat] = (catCounts[cat] || 0) + 1;

          const danger = traducirPeligro(data.dangerLevel || 'UNKNOWN');
          dangerCounts[danger] = (dangerCounts[danger] || 0) + 1;

          if (data.probability !== undefined) {
            if (!aiConfidenceMap[cat]) aiConfidenceMap[cat] = { totalProb: 0, count: 0 };
            aiConfidenceMap[cat].totalProb += data.probability;
            aiConfidenceMap[cat].count += 1;
          }

          if (currentStatus === 'REJECTED' || currentStatus === 'DELETED') {
            if (data.moderatedBy) {
              validationCounts['Rechazadas (Admin)']++;
            } else {
              validationCounts['Rechazadas (IA)']++;
            }
          } else if ((data.probability || 0) < 0.40 && !data.moderatedBy && currentStatus !== 'APPROVED') {
            validationCounts['Rechazadas (IA)']++;
          } else if (currentStatus === 'PENDING_REVIEW' || data.needsReview) {
            validationCounts['Pendientes']++;
          } else {
            validationCounts['Aprobadas']++;
          }

          if (data.timestamp) {
            const dateObj = data.timestamp.toDate ? data.timestamp.toDate() : new Date(data.timestamp);
            const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
            const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);
            monthlyCapturesMap[monthFormatted] = (monthlyCapturesMap[monthFormatted] || 0) + 1;
          }
        });

        const sessionsSnap = await getDocs(collection(db, 'sessions'));
        let totalSessions = 0;
        let totalMinutes = 0;
        const monthlySessionsMap: Record<string, number> = {};
        const timelineMap: Record<string, { name: string, minutos: number, dateObj: Date }> = {};
        const userSessionsMap: Record<string, Date[]> = {};

        sessionsSnap.forEach(doc => {
          totalSessions++;
          const data = doc.data();
          const duration = data.durationMinutes || 0;
          totalMinutes += duration;

          if (data.userId && data.startedAt) {
            const sessionDate = data.startedAt.toDate ? data.startedAt.toDate() : new Date(data.startedAt.toMillis());
            if (!userFirstSession[data.userId] || sessionDate < userFirstSession[data.userId]) {
              userFirstSession[data.userId] = sessionDate;
            }
          }

          if (data.startedAt) {
            const dateObj = data.startedAt.toDate ? data.startedAt.toDate() : new Date(data.startedAt.toMillis());
            const dateKey = dateObj.toISOString().split('T')[0];
            const dateStr = dateObj.toLocaleDateString('es-CL', { month: 'short', day: 'numeric' });
            const monthStr = dateObj.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });
            const monthFormatted = monthStr.charAt(0).toUpperCase() + monthStr.slice(1);

            if (!timelineMap[dateKey]) {
              timelineMap[dateKey] = { name: dateStr, minutos: 0, dateObj: dateObj };
            }
            timelineMap[dateKey].minutos += duration;
            monthlySessionsMap[monthFormatted] = (monthlySessionsMap[monthFormatted] || 0) + duration;

            if (data.userId) {
              if (!userSessionsMap[data.userId]) userSessionsMap[data.userId] = [];
              userSessionsMap[data.userId].push(dateObj);
            }
          }
        });

        const sortedSessionsData = Object.values(timelineMap)
          .sort((a, b) => a.dateObj.getTime() - b.dateObj.getTime())
          .map(item => ({ name: item.name, minutos: item.minutos }));

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

        let activadosCount = 0;
        let abandonoCount = 0;

        Object.keys(userFirstSession).forEach(uid => {
          const primeraSesion = userFirstSession[uid];
          const primeraCaptura = userFirstCapture[uid];

          if (primeraCaptura) {
            const diffMinutos = (primeraCaptura.getTime() - primeraSesion.getTime()) / (1000 * 60);
            if (diffMinutos <= 10 && diffMinutos >= 0) {
              activadosCount++;
            } else {
              abandonoCount++; 
            }
          } else {
            abandonoCount++; 
          }
        });

        const totalEvaluados = activadosCount + abandonoCount;
        const activadosPorc = totalEvaluados > 0 ? Math.round((activadosCount / totalEvaluados) * 100) : 0;
        setActivationStats({
          activados: activadosPorc,
          abandono: totalEvaluados > 0 ? 100 - activadosPorc : 0
        });

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
          .slice(0, 5) 
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

  // 2. EFECTO PARA FILTRADO REGIONAL
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
      const gen = traducirGenero(u.gender || 'UNSPECIFIED');
      genreCounts[gen] = (genreCounts[gen] || 0) + 1;
      const com = u.city || 'Sin comuna';
      comunaCounts[com] = (comunaCounts[com] || 0) + 1;

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
      if (a.name.includes('definido') || a.name.includes('Reservado') || a.name === 'Otro') return 1;
      if (b.name.includes('definido') || b.name.includes('Reservado') || b.name === 'Otro') return -1;
      return a.name.localeCompare(b.name);
    });

    setGenreData(genreArray);
    setComunaData(Object.entries(comunaCounts).map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value).slice(0, 10)); 
    setAgeData(Object.entries(ageCounts).map(([name, value]) => ({ name, value }))
      .filter(item => item.value > 0));

  }, [selectedRegion, rawUsers]);

  // 3. FUNCIÓN PARA EXPORTAR REPORTE
  const handleExportCSV = () => {
    let csvContent = "data:text/csv;charset=utf-8,\n";
    csvContent += "=== REPORTE DE INTELIGENCIA IBICHOS ===\n\n";
    
    csvContent += "METRICAS GLOBALES\n";
    csvContent += `Usuarios Totales,${stats.totalUsers}\n`;
    csvContent += `Capturas Totales,${stats.totalCaptures}\n`;
    csvContent += `Capturas Pendientes de Revision,${stats.pendingReview}\n`;
    csvContent += `Minutos de Uso Total,${stats.totalMinutes}\n`;
    csvContent += `Sesiones Totales,${stats.totalSessions}\n\n`;

    csvContent += "RETENCION Y ACTIVACION\n";
    csvContent += `Tasa Activacion (Primeros 10 min),${activationStats.activados}%\n`;
    csvContent += `Retencion Dia 1,${retentionStats.day1}%\n`;
    csvContent += `Retencion Dia 7,${retentionStats.day7}%\n`;
    csvContent += `Retencion Dia 30,${retentionStats.day30}%\n\n`;

    csvContent += "EFICACIA INTELIGENCIA ARTIFICIAL\n";
    csvContent += "Estado,Cantidad\n";
    validationData.forEach(d => { csvContent += `${d.name},${d.value}\n`; });
    csvContent += "\n";

    csvContent += "BIODIVERSIDAD (ESPECIES IDENTIFICADAS)\n";
    csvContent += "Categoria,Cantidad\n";
    categoryData.forEach(d => { csvContent += `${d.name},${d.value}\n`; });
    csvContent += "\n";

    csvContent += "SALUD PUBLICA (NIVEL DE RIESGO)\n";
    csvContent += "Nivel,Cantidad\n";
    dangerData.forEach(d => { csvContent += `${d.name},${d.value}\n`; });
    csvContent += "\n";

    csvContent += `DEMOGRAFIA (Filtro: ${selectedRegion})\n`;
    csvContent += "Rango Etario,Cantidad\n";
    ageData.forEach(d => { csvContent += `${d.name},${d.value}\n`; });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    const dateStr = new Date().toLocaleDateString('es-CL').replace(/\//g, '-');
    link.setAttribute("download", `Reporte_iBichos_${dateStr}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // 4. FUNCIÓN PARA GUARDAR SNAPSHOT EN FIRESTORE
  const handleSaveSnapshot = async (isAutoSave = false) => {
    try {
      const now = new Date();
      const monthStr = now.toISOString().slice(0, 7); 
      
      const snapshotData = {
        saveDate: now.toISOString(),
        month: monthStr,
        globalMetrics: stats,
        retention: retentionStats,
        activation: activationStats,
        aiEfficacy: validationData,
        biodiversity: categoryData,
        publicHealth: dangerData,
        communityHealth: shadowbanData
      };

      await setDoc(doc(db, 'historical_reports', monthStr), snapshotData);
      
      if (!isAutoSave) {
        setShowSuccessModal(true);
      } else {
        console.log(`Auto-guardado de reporte mensual exitoso: ${monthStr}`);
      }
    } catch (error) {
      console.error("Error al guardar la instantánea en Firestore: ", error);
      if (!isAutoSave) alert("❌ Hubo un error al guardar el reporte en la nube.");
    }
  };

  // 5. EFECTO DE AUTOGUARDADO MENSUAL
  useEffect(() => {
    const autoSaveMonthly = async () => {
      if (loading || stats.totalUsers === 0) return;
      try {
        const now = new Date();
        const monthStr = now.toISOString().slice(0, 7);
        const docRef = doc(db, 'historical_reports', monthStr);
        const docSnap = await getDoc(docRef);
        if (!docSnap.exists()) {
          await handleSaveSnapshot(true);
        }
      } catch (e) {
        console.error("Error en autoguardado:", e);
      }
    };
    autoSaveMonthly();
  }, [loading, stats]);

  // 6. EL RETURN QUE EXPONE TODO A LA VISTA
  return {
    activeTab, setActiveTab,
    loading,
    showSuccessModal, setShowSuccessModal,
    stats, levelData, categoryData, dangerData, sessionsData, 
    retentionStats, activationStats, validationData, shadowbanData, 
    aiConfidenceData, medalsData, genreData, ageData,
    regionsList, selectedRegion, setSelectedRegion,
    handleExportCSV, handleSaveSnapshot
  };
}
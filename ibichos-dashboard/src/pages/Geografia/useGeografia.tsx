import { useEffect, useState } from 'react';
import { db } from '../../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';

export function useGeografia() {
  const [loading, setLoading] = useState(true);
  const [regionData, setRegionData] = useState<any[]>([]);
  const [comunaData, setComunaData] = useState<any[]>([]);
  const [topComunasLimit, setTopComunasLimit] = useState<number>(5);
  const [stats, setStats] = useState({ topRegion: '...', topComuna: '...', totalLocs: 0 });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const usersSnap = await getDocs(collection(db, 'users'));

        const regionCounts: Record<string, number> = {};
        const comunaCounts: Record<string, number> = {};
        let totalUsers = 0;

        usersSnap.forEach(doc => {
          const data = doc.data();
          totalUsers++;

          const reg = data.region || 'Sin Región';
          const com = data.city || 'Sin Comuna';

          regionCounts[reg] = (regionCounts[reg] || 0) + 1;
          comunaCounts[com] = (comunaCounts[com] || 0) + 1;
        });

        const sortedRegions = Object.entries(regionCounts)
          .map(([name, value]) => ({ name, value }))
          .sort((a, b) => b.value - a.value);

        const sortedComunas = Object.entries(comunaCounts)
          .map(([name, value]) => ({ name, value }))
          .sort((a, b) => b.value - a.value);

        setRegionData(sortedRegions);
        setComunaData(sortedComunas); 

        setStats({
          topRegion: sortedRegions[0]?.name || 'N/A',
          topComuna: sortedComunas[0]?.name || 'N/A',
          totalLocs: Object.keys(comunaCounts).length
        });

      } catch (error) {
        console.error("Error al cargar geografía:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return {
    loading,
    regionData,
    comunaData,
    topComunasLimit, setTopComunasLimit,
    stats
  };
}
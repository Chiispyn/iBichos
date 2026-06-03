import { useState, useEffect } from 'react';
import { db } from '../../config/firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';

export function usePrincipal() {
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

        // Fetch Usuarios
        const usersSnap = await getDocs(collection(db, 'users'));
        usersSnap.forEach(doc => {
          const data = doc.data();
          if (data.createdAt) {
            const fechaReg = new Date(data.createdAt.toMillis()).toLocaleDateString('es-CL');
            if (fechaReg === hoyString) nuevosUsuarios++;
          }
        });

        // Fetch Capturas
        const capturesSnap = await getDocs(collection(db, 'captures'));
        capturesSnap.forEach(doc => {
          const data = doc.data();
          if (data.timestamp) {
            const fechaCap = new Date(data.timestamp.toMillis()).toLocaleDateString('es-CL');
            if (fechaCap === hoyString) {
              const status = data.validationStatus || data.status;
              if (status === 'APPROVED') {
                capturasHoy++;
              }
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
  }, []); // El warning de dependencias de `hoy` se puede ignorar aquí, o puedes meter `hoy` dentro del useEffect si tu linter molesta.

  return {
    statsDia,
    cargando,
    fechaFormateada
  };
}
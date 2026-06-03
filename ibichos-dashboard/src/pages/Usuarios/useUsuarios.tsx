// src/pages/Usuarios/useUsuarios.ts
import { useState, useEffect } from 'react';
import { collection, onSnapshot } from 'firebase/firestore';
import { db } from '../../config/firebaseConfig';
import type { Usuario } from '../../types/usuario';

export function useUsuarios() {
  const [listaUsuarios, setListaUsuarios] = useState<Usuario[]>([]);
  const [adminsIds, setAdminsIds] = useState<string[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setCargando(true);

    // Diccionarios de traducción
    const traducirGenero = (g: string) => {
      const dict: Record<string, string> = { MALE: 'Masculino', FEMALE: 'Femenino', OTHER: 'Otro', PREFER_NOT_TO_SAY: 'Reservado', UNSPECIFIED: 'No definido' };
      return dict[g] || g;
    };

    const traducirNivel = (lvl: string) => {
      const dict: Record<string, string> = { CASUAL: 'Casual', AMATEUR: 'Amateur', EXPLORER: 'Explorador', ENTOMOLOGIST: 'Entomólogo', BUG_MASTER: 'Maestro de Bichos' };
      return dict[lvl] || lvl;
    };

    // 1. Escucha de Admins
    const unsubscribeAdmins = onSnapshot(collection(db, "admins"), (snap) => {
      setAdminsIds(snap.docs.map(doc => doc.id));
    });

    // 2. Escucha de Usuarios
    const unsubscribeUsers = onSnapshot(collection(db, "users"), (snap) => {
      const usuariosMapeados: Usuario[] = snap.docs.map(doc => {
        const data = doc.data();
        return {
          id: doc.id,
          username: data.displayName || 'Sin nombre',
          genre: traducirGenero(data.gender || 'UNSPECIFIED'),
          email: data.email || '',
          birthdate: data.birthDate || 'N/A',
          region: data.region || 'Sin región',
          comuna: data.city || 'Sin comuna',
          level: traducirNivel(data.gamification?.level || 'CASUAL'),
          xp: data.xp || 0,
          createdAt: data.createdAt ? new Date(data.createdAt.toMillis()).toLocaleDateString() : undefined,
          isShadowBanned: data.isShadowBanned || false,
          strikes: data.strikes || 0
        };
      });
      setListaUsuarios(usuariosMapeados);
      setCargando(false);
    }, (error) => {
      console.error("Error en tiempo real (Usuarios):", error);
      setCargando(false);
    });

    return () => {
      unsubscribeAdmins();
      unsubscribeUsers();
    };
  }, []);

  // Retornamos solo lo que la vista necesita
  return {
    listaUsuarios,
    adminsIds,
    cargando
  };
}
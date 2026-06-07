// src/__tests__/pages/Principal/usePrincipal.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { usePrincipal } from '../../../pages/Principal/usePrincipal';
import { getDocs } from 'firebase/firestore';

// 1. MOCK DE FIREBASE
vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn((_db, collectionName) => ({ _id: collectionName })),
    getDocs: vi.fn(),
    getFirestore: vi.fn()
  };
});

// Mock de la configuración de Firebase
vi.mock('../../../config/firebaseConfig', () => ({
  db: {}
}));

describe('TC-62 — Cerebro Lógico Principal (usePrincipal)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();

    // Interceptamos getDocs para devolver datos distintos según la colección
    vi.mocked(getDocs).mockImplementation(async (colRef: any) => {
      const hoy = new Date(); // Simulamos "hoy" en base a la fecha de ejecución del test

      if (colRef._id === 'users') {
        return {
          forEach: (cb: any) => {
            // Usuario registrado "hoy"
            cb({ data: () => ({ createdAt: { toMillis: () => hoy.getTime() } }) });
            // Usuario registrado ayer
            cb({ data: () => ({ createdAt: { toMillis: () => hoy.getTime() - 86400000 } }) });
          }
        } as any;
      }

      if (colRef._id === 'captures') {
        return {
          forEach: (cb: any) => {
            // Captura aprobada "hoy"
            cb({ data: () => ({ validationStatus: 'APPROVED', timestamp: { toMillis: () => hoy.getTime() } }) });
            // Captura pendiente "hoy"
            cb({ data: () => ({ status: 'PENDING_REVIEW', timestamp: { toMillis: () => hoy.getTime() } }) });
            // Captura aprobada ayer (no debe contarse)
            cb({ data: () => ({ status: 'APPROVED', timestamp: { toMillis: () => hoy.getTime() - 86400000 } }) });
          }
        } as any;
      }

      return { forEach: () => {} } as any;
    });
  });

  test('Inicializa en estado de carga y devuelve la fecha formateada', () => {
    const { result } = renderHook(() => usePrincipal());
    
    expect(result.current.cargando).toBe(true);
    expect(result.current.fechaFormateada).toBeDefined();
    expect(typeof result.current.fechaFormateada).toBe('string');
  });

  test('Calcula correctamente las métricas filtrando por la fecha de "hoy"', async () => {
    const { result } = renderHook(() => usePrincipal());
    
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // Validamos las métricas según los mocks que inyectamos arriba
    // Solo 1 usuario registrado hoy
    expect(result.current.statsDia.nuevosUsuarios).toBe(1);
    
    // Solo 1 captura aprobada hoy (la de ayer no cuenta)
    expect(result.current.statsDia.capturasHoy).toBe(1);
    
    // Solo 1 captura pendiente de revisión hoy
    expect(result.current.statsDia.pendientesHoy).toBe(1);
  });
});
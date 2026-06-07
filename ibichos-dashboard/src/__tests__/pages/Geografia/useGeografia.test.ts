// src/__tests__/pages/Geografia/useGeografia.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useGeografia } from '../../../pages/Geografia/useGeografia';

// 1. MOCK DE FIREBASE
vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn(),
    getDocs: vi.fn(async () => ({
      forEach: (cb: any) => {
        // Simulamos usuarios distribuidos en diferentes zonas
        cb({ data: () => ({ region: 'Biobío', city: 'Concepción' }) });
        cb({ data: () => ({ region: 'Biobío', city: 'Talcahuano' }) });
        cb({ data: () => ({ region: 'Biobío', city: 'Concepción' }) });
        cb({ data: () => ({ region: 'RM', city: 'Santiago' }) });
        cb({ data: () => ({ /* Usuario anónimo sin región ni ciudad */ }) });
      }
    })),
    getFirestore: vi.fn()
  };
});

// 2. MOCK DE LA CONFIGURACIÓN DE FIREBASE
vi.mock('../../../config/firebaseConfig', () => ({
  db: {}
}));

describe('TC-59 — Cerebro Lógico de Geografía (useGeografia)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Inicializa en estado de carga y luego procesa los datos', async () => {
    const { result } = renderHook(() => useGeografia());
    
    // Al principio debe estar cargando
    expect(result.current.loading).toBe(true);

    // Esperamos a que termine el fetch
    await waitFor(() => expect(result.current.loading).toBe(false));
  });

  test('Agrupa, suma y ordena las regiones y comunas correctamente', async () => {
    const { result } = renderHook(() => useGeografia());
    await waitFor(() => expect(result.current.loading).toBe(false));

    // Validamos REGIONES
    // Biobío tiene 3 usuarios, RM tiene 1, Sin Región tiene 1
    expect(result.current.regionData).toHaveLength(3);
    expect(result.current.regionData[0]).toEqual({ name: 'Biobío', value: 3 }); // Top 1
    
    // Validamos COMUNAS
    // Concepción tiene 2, Talcahuano 1, Santiago 1, Sin Comuna 1
    expect(result.current.comunaData).toHaveLength(4);
    expect(result.current.comunaData[0]).toEqual({ name: 'Concepción', value: 2 }); // Top 1
  });

  test('Calcula las estadísticas globales (stats) para las tarjetas de resumen', async () => {
    const { result } = renderHook(() => useGeografia());
    await waitFor(() => expect(result.current.loading).toBe(false));

    // Verifica que el cálculo determinó a Biobío y Concepción como los líderes
    expect(result.current.stats.topRegion).toBe('Biobío');
    expect(result.current.stats.topComuna).toBe('Concepción');
    
    // Verifica que contó 4 zonas distintas (Concepción, Talcahuano, Santiago, Sin Comuna)
    expect(result.current.stats.totalLocs).toBe(4);
  });
});
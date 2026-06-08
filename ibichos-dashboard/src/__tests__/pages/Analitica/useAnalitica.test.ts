// src/__tests__/pages/Analitica/useAnalitica.test.ts
import { renderHook, waitFor, act } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useAnalitica } from '../../../pages/Analitica/useAnalitica';
import { getDocs, setDoc, getDoc } from 'firebase/firestore';

// MOCK DE FIREBASE CON IDENTIFICADOR DE COLECCIONES
vi.mock('firebase/firestore', () => ({
  collection: vi.fn((_db, path) => ({ id: path })),
  getDocs: vi.fn(),
  doc: vi.fn(),
  setDoc: vi.fn(),
  getDoc: vi.fn()
}));

vi.mock('../../../config/firebaseConfig', () => ({ db: {} }));

describe('TC-66 — Cerebro Lógico de Analítica (useAnalitica)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // 🔥 INYECCIÓN DE DATOS MASIVOS: Simulamos que Firebase devuelve datos reales
    vi.mocked(getDocs).mockImplementation(async (query: any) => {
      const path = query.id;
      const hoy = new Date();
      
      if (path === 'users') {
         return { forEach: (cb: any) => {
           cb({ data: () => ({ region: 'Biobío', city: 'Florida', gender: 'MALE', birthDate: '15/05/1996', gamification: { level: 'EXPLORER', medals: ['FIRST_CAPTURE'] } }) });
           cb({ data: () => ({ region: 'Metropolitana', isShadowBanned: true }) });
         }} as any;
      }
      if (path === 'captures') {
         return { forEach: (cb: any) => {
           cb({ data: () => ({ userId: '1', status: 'APPROVED', category: 'ARACHNIDA', dangerLevel: 'VENOMOUS', probability: 0.9, timestamp: { toDate: () => hoy } }) });
           cb({ data: () => ({ status: 'REJECTED', moderatedBy: 'admin', probability: 0.2, timestamp: { toDate: () => hoy } }) });
           cb({ data: () => ({ status: 'PENDING_REVIEW', timestamp: { toDate: () => hoy } }) });
         }} as any;
      }
      if (path === 'sessions') {
         return { forEach: (cb: any) => {
           cb({ data: () => ({ userId: '1', durationMinutes: 20, startedAt: { toDate: () => hoy } }) });
         }} as any;
      }
      return { forEach: () => {} } as any;
    });
    
    vi.mocked(getDoc).mockResolvedValue({ exists: () => false } as any);
  });

  test('Inicializa y procesa las matemáticas con los datos simulados', async () => {
    const { result } = renderHook(() => useAnalitica());
    expect(result.current.loading).toBe(true);
    await waitFor(() => expect(result.current.loading).toBe(false));
    
    // Verificamos que procesó los 2 usuarios y 3 capturas del mock
    expect(result.current.stats.totalUsers).toBe(2);
    expect(result.current.stats.totalCaptures).toBe(3);
  });

  test('Filtra la demografía correctamente al cambiar de región', async () => {
    const { result } = renderHook(() => useAnalitica());
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setSelectedRegion('Biobío');
    });
    expect(result.current.selectedRegion).toBe('Biobío');
  });

  test('handleExportCSV genera y descarga el archivo correctamente', async () => {
    const { result } = renderHook(() => useAnalitica());
    await waitFor(() => expect(result.current.loading).toBe(false));

    const createElementSpy = vi.spyOn(document, 'createElement');
    const appendChildSpy = vi.spyOn(document.body, 'appendChild').mockImplementation((node: any) => node);
    const removeChildSpy = vi.spyOn(document.body, 'removeChild').mockImplementation((node: any) => node);

    act(() => { result.current.handleExportCSV(); });
    expect(createElementSpy).toHaveBeenCalledWith('a');
    
    createElementSpy.mockRestore();
    appendChildSpy.mockRestore();
    removeChildSpy.mockRestore();
  });

  test('handleSaveSnapshot guarda los datos en Firestore y muestra el modal', async () => {
    const { result } = renderHook(() => useAnalitica());
    await waitFor(() => expect(result.current.loading).toBe(false));

    // 🚨 El auto-guardado (useEffect) ya disparó setDoc una vez al terminar de cargar.
    // Limpiamos el historial del espía para contar solo nuestro click manual.
    vi.mocked(setDoc).mockClear();

    await act(async () => { await result.current.handleSaveSnapshot(false); });
    
    // Ahora sí será exactamente 1
    expect(setDoc).toHaveBeenCalledTimes(1);
    expect(result.current.showSuccessModal).toBe(true);
  });
});
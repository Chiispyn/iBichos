// src/__tests__/pages/Capturas/useCapturas.test.ts
import { renderHook, waitFor, act } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useCapturas } from '../../../pages/Capturas/useCapturas';
import { updateDoc, setDoc } from 'firebase/firestore';

vi.mock('../../../context/authcontext', () => ({
  useAuth: () => ({ user: { uid: 'admin_123', email: 'admin@bichos.cl' } })
}));

vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn(),
    doc: vi.fn((_db, collectionName, id) => ({ _id: id })), 
    updateDoc: vi.fn(),
    setDoc: vi.fn(),
    increment: vi.fn((val) => val),
    getDocs: vi.fn(async () => ({
      forEach: (cb: any) => {
        cb({ id: 'user_felipe', data: () => ({ displayName: 'Felipe', email: 'felipe@test.cl' }) });
      }
    })),
    onSnapshot: vi.fn((_query, callback) => {
      callback({
        docs: [
          {
            id: 'cap_1',
            data: () => ({ status: 'PENDING_REVIEW', insectName: 'Hormiga', timestamp: { toDate: () => new Date() } })
          }
        ]
      });
      return vi.fn();
    }),
    getFirestore: vi.fn()
  };
});

vi.mock('../../../config/firebaseConfig', () => ({ db: {} }));

describe('TC-57 — Cerebro Lógico de Capturas (useCapturas)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Escucha en tiempo real y clasifica las capturas', async () => {
    const { result } = renderHook(() => useCapturas());
    await waitFor(() => expect(result.current.cargando).toBe(false));
    expect(result.current.filtro).toBe('PENDING_REVIEW');
    expect(result.current.capturasFiltradas).toHaveLength(1);
  });

  test('Aplica una sanción (Strike) y actualiza la base de datos', async () => {
    const { result } = renderHook(() => useCapturas());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    vi.spyOn(window, 'alert').mockImplementation(() => {});

    await act(async () => {
      await result.current.handleModeracion('cap_1', 'REJECTED', undefined, true);
    });

    expect(updateDoc).toHaveBeenCalled();
    expect(setDoc).toHaveBeenCalled();
  });

  // 👇 TEST NUEVO: Ejecuta la función huérfana de handleEliminar (Líneas 142-172)
  test('Abre el modal de eliminación y ejecuta handleEliminar', async () => {
    const { result } = renderHook(() => useCapturas());
    await waitFor(() => expect(result.current.cargando).toBe(false));
    
    vi.spyOn(window, 'alert').mockImplementation(() => {});

    // 1. Abrimos el modal simulando el click en el basurero
    act(() => { result.current.openModal('cap_1'); });
    expect(result.current.selectedId).toBe('cap_1');
    expect(result.current.showModal).toBe(true);

    // 2. Ejecutamos la función de eliminar
    await act(async () => { await result.current.handleEliminar(); });
    
    // Verificamos que impactó en la BD y cerró el modal
    expect(updateDoc).toHaveBeenCalled();
    expect(result.current.showModal).toBe(false);
    expect(result.current.selectedId).toBe(null);
  });

  // 👇 TEST NUEVO: Cubre los casos "default" de los colores (Líneas 11-23)
  test('Funciones de formateo devuelven los valores por defecto y correctos', () => {
    const { result } = renderHook(() => useCapturas());
    expect(result.current.getDangerColor('VENOMOUS')).toBe('danger');
    expect(result.current.getDangerColor('UNKNOWN')).toBe('secondary'); // Caso default
    
    expect(result.current.traducirPeligro('HARMLESS')).toBe('Inofensivo');
    expect(result.current.traducirPeligro('UNKNOWN')).toBe('Desconocido'); // Caso default
  });
});
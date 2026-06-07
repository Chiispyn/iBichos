// src/__tests__/pages/Catalogo/useCatalogo.test.ts
import { renderHook, waitFor, act } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useCatalogo } from '../../../pages/Catalogo/useCatalogo';
import { updateDoc, setDoc } from 'firebase/firestore';

// 1. MOCK DE AUTENTICACIÓN
vi.mock('../../../context/authcontext', () => ({
  useAuth: () => ({ user: { uid: 'admin_1', email: 'admin@test.cl' } })
}));

// 2. MOCK DE FIREBASE
vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn(),
    doc: vi.fn((_db, collectionName, id) => ({ _id: id })), 
    updateDoc: vi.fn(),
    setDoc: vi.fn(),
    getDocs: vi.fn(async () => ({
      forEach: (cb: any) => {
        // Simulamos usuarios para el userMap
        cb({ id: 'user1', data: () => ({ displayName: 'Felipe', email: 'felipe@test.com' }) });
        cb({ id: 'user2', data: () => ({ displayName: 'Ana', email: 'ana@test.com' }) });
      }
    })),
    onSnapshot: vi.fn((_query, callback) => {
      callback({
        docs: [
          {
            id: 'cap_1',
            data: () => ({
              status: 'APPROVED',
              insectName: 'Mariposa Monarca',
              scientificName: 'Danaus plexippus',
              userId: 'user1',
              timestamp: { toDate: () => new Date('2026-06-06T10:00:00Z') }
            })
          },
          {
            id: 'cap_2',
            data: () => ({
              status: 'PENDING_REVIEW', // <-- Esta NO debe aparecer en el catálogo
              insectName: 'Araña Pollito',
              scientificName: 'Grammostola rosea',
              userId: 'user2',
              timestamp: { toDate: () => new Date('2026-06-05T10:00:00Z') }
            })
          },
          {
            id: 'cap_3',
            data: () => ({
              status: 'APPROVED',
              insectName: 'Escarabajo Rinoceronte',
              scientificName: 'Oryctes nasicornis',
              userId: 'user1',
              timestamp: { toDate: () => new Date('2026-06-04T10:00:00Z') }
            })
          }
        ]
      });
      return vi.fn();
    }),
    getFirestore: vi.fn()
  };
});

vi.mock('../../../config/firebaseConfig', () => ({ db: {} }));

describe('TC-58 — Cerebro Lógico del Catálogo (useCatalogo)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC-30 (Refactor Lógico) — Solo mapea capturas APPROVED al catálogo', async () => {
    const { result } = renderHook(() => useCatalogo());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // De las 3 capturas del mock, solo 2 son APPROVED
    expect(result.current.filtered).toHaveLength(2);
    expect(result.current.filtered.every(c => c.status === 'APPROVED')).toBe(true);
  });

  test('TC-31 (Refactor Lógico) — Motor de búsqueda por nombre de insecto o científico', async () => {
    const { result } = renderHook(() => useCatalogo());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // Simulamos que el admin busca "Mariposa"
    act(() => {
      result.current.setBusqueda('Mariposa');
    });

    expect(result.current.filtered).toHaveLength(1);
    expect(result.current.filtered[0].insectName).toBe('Mariposa Monarca');

    // Simulamos búsqueda por nombre científico
    act(() => {
      result.current.setBusqueda('Oryctes');
    });

    expect(result.current.filtered).toHaveLength(1);
    expect(result.current.filtered[0].scientificName).toBe('Oryctes nasicornis');
  });

  test('TC-32 (Refactor Lógico) — Búsqueda transversal por nombre de usuario o email', async () => {
    const { result } = renderHook(() => useCatalogo());
    
    // Esperamos a que cargue tanto el catálogo como el mapa de usuarios
    await waitFor(() => expect(result.current.cargando).toBe(false));
    await waitFor(() => expect(Object.keys(result.current.userMap).length).toBeGreaterThan(0));

    // Simulamos que el admin busca "Felipe" (que tiene 2 capturas aprobadas)
    act(() => {
      result.current.setBusqueda('Felipe');
    });

    expect(result.current.filtered).toHaveLength(2);

    // Simulamos búsqueda por el correo
    act(() => {
      result.current.setBusqueda('felipe@test.com');
    });

    expect(result.current.filtered).toHaveLength(2);
  });

 test('Las modificaciones al catálogo disparan las escrituras correctas en Firebase', async () => {
    const { result } = renderHook(() => useCatalogo());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // Test actualización
    await act(async () => {
      await result.current.handleUpdate('cap_1', { category: 'LEPIDOPTERA' });
    });
    expect(updateDoc).toHaveBeenCalled();
    expect(setDoc).toHaveBeenCalled(); // Se guardó el log

    // Forzamos targetId para rechazar (¡DEBE estar en act() para que React actualice el estado!)
    act(() => {
      result.current.openDeleteModal('cap_1');
    });

    await act(async () => {
      await result.current.handleReject();
    });
    expect(updateDoc).toHaveBeenCalledTimes(2); // ¡Ahora sí se ejecutará la segunda vez!
  });
});
// src/__tests__/pages/Usuarios/useUsuarios.test.ts
import { renderHook, waitFor, act } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useUsuarios } from '../../../pages/Usuarios/useUsuarios';
import { setDoc, deleteDoc } from 'firebase/firestore';

// 1. MOCK DE AUTENTICACIÓN
vi.mock('../../../context/authcontext', () => ({
  useAuth: () => ({ user: { uid: 'superadmin', email: 'admin@bichos.cl' } })
}));

// 2. MOCK DE FIREBASE (SIMULANDO TIEMPO REAL DOBLE)
vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn((_db, collectionName) => ({ _id: collectionName })),
    doc: vi.fn((_db, collectionName, id) => ({ _id: id })), 
    setDoc: vi.fn(),
    deleteDoc: vi.fn(),
    onSnapshot: vi.fn((colRef, callback) => {
      // Si el hook pide la colección 'admins', le damos 1 administrador
      if (colRef._id === 'admins') {
        callback({ docs: [{ id: 'user_1' }] });
      } 
      // Si el hook pide 'users', le damos 3 usuarios de prueba
      else if (colRef._id === 'users') {
        callback({
          docs: [
            {
              id: 'user_1', // Este es el admin
              data: () => ({ displayName: 'Felipe', xp: 5000, gamification: { level: 'ENTOMOLOGIST' } })
            },
            {
              id: 'user_2', // Usuario baneado
              data: () => ({ displayName: 'Juan', xp: 120, isShadowBanned: true })
            },
            {
              id: 'user_3', // Usuario normal
              data: () => ({ displayName: 'Ana', xp: 300, isShadowBanned: false })
            }
          ]
        });
      }
      return vi.fn(); // Función unsubscribe
    }),
    getFirestore: vi.fn()
  };
});

vi.mock('../../../config/firebaseConfig', () => ({ db: {} }));

describe('TC-63 — Cerebro Lógico de Usuarios (useUsuarios)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Mapea las colecciones en tiempo real y ordena por XP por defecto', async () => {
    const { result } = renderHook(() => useUsuarios());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // Debe haber 3 usuarios en total
    expect(result.current.usuariosProcesados).toHaveLength(3);
    
    // Por defecto, ordenDireccion es 'desc' y ordenColumna es 'xp'. 
    // Por ende, Felipe (5000) > Ana (300) > Juan (120)
    expect(result.current.usuariosProcesados[0].username).toBe('Felipe');
    expect(result.current.usuariosProcesados[1].username).toBe('Ana');
    expect(result.current.usuariosProcesados[2].username).toBe('Juan');
  });

  test('Filtra correctamente usando las pestañas (Tabs)', async () => {
    const { result } = renderHook(() => useUsuarios());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    // Probamos pestaña Moderadores
    act(() => result.current.setFiltroTab('Moderadores'));
    expect(result.current.usuariosProcesados).toHaveLength(1);
    expect(result.current.usuariosProcesados[0].username).toBe('Felipe');

    // Probamos pestaña Baneados
    act(() => result.current.setFiltroTab('Baneados'));
    expect(result.current.usuariosProcesados).toHaveLength(1);
    expect(result.current.usuariosProcesados[0].username).toBe('Juan');

    // Probamos pestaña Jugadores (No admins)
    act(() => result.current.setFiltroTab('Jugadores'));
    expect(result.current.usuariosProcesados).toHaveLength(2); // Ana y Juan
  });

  test('El motor de búsqueda filtra por nombre insensible a mayúsculas', async () => {
    const { result } = renderHook(() => useUsuarios());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    act(() => result.current.setBusqueda('aNa')); // Búsqueda mixta
    expect(result.current.usuariosProcesados).toHaveLength(1);
    expect(result.current.usuariosProcesados[0].username).toBe('Ana');
  });

  test('handleHacerAdmin prepara el modal y, al confirmar, guarda en Firebase', async () => {
    const { result } = renderHook(() => useUsuarios());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    const usuarioNormal = result.current.usuariosProcesados.find(u => u.username === 'Ana')!;

    // 1. Disparamos la acción de hacer admin
    act(() => {
      result.current.handleHacerAdmin(usuarioNormal);
    });

    // 2. Verificamos que el modal se abrió con la función de confirmación inyectada
    expect(result.current.modal.isOpen).toBe(true);
    expect(result.current.modal.onConfirm).toBeTypeOf('function');

    // 3. Simulamos que el administrador presiona "Confirmar" en el modal
    await act(async () => {
      if (result.current.modal.onConfirm) {
        await result.current.modal.onConfirm();
      }
    });

    // 4. Verificamos que se escribieron los datos en Firestore (1 para hacer admin, 1 para el log de auditoría)
    expect(setDoc).toHaveBeenCalledTimes(2);
  });

  test('handleQuitarAdmin prepara el modal y, al confirmar, elimina de Firebase', async () => {
    const { result } = renderHook(() => useUsuarios());
    await waitFor(() => expect(result.current.cargando).toBe(false));

    const adminUser = result.current.usuariosProcesados.find(u => u.username === 'Felipe')!;

    act(() => {
      result.current.handleQuitarAdmin(adminUser);
    });

    await act(async () => {
      if (result.current.modal.onConfirm) {
        await result.current.modal.onConfirm();
      }
    });

    // Verifica que se eliminó de la tabla 'admins' y se generó el log de auditoría
    expect(deleteDoc).toHaveBeenCalledTimes(1);
    expect(setDoc).toHaveBeenCalledTimes(1); 
  });
});
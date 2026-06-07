// src/__tests__/pages/Auditoria/useAuditoria.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { useAuditoria } from '../../../pages/Auditoria/useAuditoria';

// 1. MOCK DE FIREBASE (SIMULANDO TIEMPO REAL)
vi.mock('firebase/firestore', () => {
  return {
    collection: vi.fn(),
    query: vi.fn(),
    orderBy: vi.fn(),
    // onSnapshot no es una promesa, es un callback listener
    onSnapshot: vi.fn((_query, callback) => {
      // Simulamos que Firebase empuja datos al instante
      callback({
        docs: [
          {
            id: 'log_abc',
            data: () => ({
              adminId: 'admin_1',
              adminEmail: 'admin@bichos.cl',
              action: 'STRIKE',
              targetId: 'post_123',
              targetType: 'POST',
              timestamp: { toDate: () => new Date('2026-06-05T10:00:00Z') }
            })
          }
        ]
      });
      // Devolvemos la función de desuscripción (unsubscribe)
      return vi.fn(); 
    }),
    getFirestore: vi.fn()
  };
});

// Mock de la configuración de Firebase
vi.mock('../../../config/firebaseConfig', () => ({
  db: {}
}));

describe('TC-55 — Cerebro Lógico de Auditoría (useAuditoria)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Recibe y mapea los logs de Firebase en tiempo real a través de onSnapshot', async () => {
    // Renderizamos el hook aislado
    const { result } = renderHook(() => useAuditoria());

    // Esperamos que el listener de Firebase dispare el cambio de estado
    await waitFor(() => {
      expect(result.current.cargando).toBe(false);
    });

    // Verificamos que el mapeo de los datos del snapshot fue exitoso
    expect(result.current.logs).toHaveLength(1);
    expect(result.current.logs[0].id).toBe('log_abc');
    expect(result.current.logs[0].adminEmail).toBe('admin@bichos.cl');
    expect(result.current.logs[0].action).toBe('STRIKE');
    
    // Validamos que el timestamp de Firebase se convirtió a un objeto Date nativo de JS
    expect(result.current.logs[0].timestamp).toBeInstanceOf(Date);
  });
});
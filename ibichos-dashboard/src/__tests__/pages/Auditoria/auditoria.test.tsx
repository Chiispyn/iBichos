// src/__tests__/pages/Auditoria/auditoria.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import Auditoria from '../../../pages/Auditoria/auditoria';
import { useAuditoria } from '../../../pages/Auditoria/useAuditoria';
import * as auditoriaUtils from '../../../pages/Auditoria/auditoriaUtils';

// 1. MOCK DEL HOOK
vi.mock('../../../pages/Auditoria/useAuditoria');

// Mockeamos las funciones de utilidad para controlar qué dibujan
vi.mock('../../../pages/Auditoria/auditoriaUtils', () => ({
  getActionIcon: vi.fn(() => <span data-testid="mock-icon">Icono</span>),
  getActionLabel: vi.fn((action) => action === 'APPROVE' ? 'Aprobado' : 'Sancionado')
}));

describe('TC-54 — Módulo de Auditoría (Interfaz y UI)', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Muestra el spinner mientras los registros están cargando', () => {
    // Simulamos que el hook está trabajando
    (useAuditoria as any).mockReturnValue({ logs: [], cargando: true });
    
    render(<Auditoria />);
    
    expect(screen.getByText('Cargando registros...')).toBeInTheDocument();
  });

  test('Muestra el estado vacío cuando no hay registros de auditoría', () => {
    // Simulamos que el hook terminó, pero no encontró datos
    (useAuditoria as any).mockReturnValue({ logs: [], cargando: false });
    
    render(<Auditoria />);
    
    expect(screen.getByText('No hay registros de auditoría')).toBeInTheDocument();
    expect(screen.getByText('Aún no se ha realizado ninguna acción de moderación.')).toBeInTheDocument();
  });

  test('Renderiza la tabla con los logs de auditoría correctamente formateados', () => {
    // Simulamos que el hook devolvió un registro de moderación
    const mockDate = new Date('2026-06-06T15:30:00');
    (useAuditoria as any).mockReturnValue({
      cargando: false,
      logs: [
        {
          id: 'log_123',
          timestamp: mockDate,
          action: 'APPROVE',
          adminEmail: 'felipe@admin.cl',
          adminId: 'admin_001',
          targetType: 'USER',
          targetId: 'user_999'
        }
      ]
    });
    
    render(<Auditoria />);
    
    // Verificamos cabeceras
    expect(screen.getByText('Registro de Auditoría')).toBeInTheDocument();
    
    // Verificamos los datos de la fila de la tabla
    expect(screen.getByText('felipe@admin.cl')).toBeInTheDocument();
    expect(screen.getByText('admin_001')).toBeInTheDocument(); // ID del admin
    expect(screen.getByText('user_999')).toBeInTheDocument(); // ID del afectado
    
    // Verificamos que las utilidades de formateo se hayan renderizado
    expect(screen.getByText('Aprobado')).toBeInTheDocument();
  });
});
import { render, screen } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import { ProtectedRoute } from '../../components/protectedRoute'; 
import { useAuth } from '../../context/authcontext';

// 1. Mockeamos el contexto de autenticación (RUTA CORREGIDA A ../../)
vi.mock('../../context/authcontext', () => ({
  useAuth: vi.fn()
}));

// 2. Mockeamos el componente <Navigate />
vi.mock('react-router-dom', () => ({
  Navigate: ({ to, replace }: any) => (
    <div data-testid="mock-navigate" data-to={to} data-replace={replace?.toString()} />
  )
}));

describe('Componente: ProtectedRoute', () => {
  test('Cubre estado de carga: Muestra texto de "Cargando sesión..."', () => {
    (useAuth as any).mockReturnValue({ loading: true, user: null, isAdminActive: false });
    
    render(
      <ProtectedRoute>
        <div>Contenido Protegido</div>
      </ProtectedRoute>
    );
    
    expect(screen.getByText('Cargando sesión...')).toBeInTheDocument();
  });

  test('Cubre rechazo: Redirige a "/" si NO hay usuario', () => {
    (useAuth as any).mockReturnValue({ loading: false, user: null, isAdminActive: false });
    
    render(
      <ProtectedRoute>
        <div>Contenido Protegido</div>
      </ProtectedRoute>
    );
    
    const navigateComponent = screen.getByTestId('mock-navigate');
    expect(navigateComponent).toBeInTheDocument();
    expect(navigateComponent).toHaveAttribute('data-to', '/');
    expect(navigateComponent).toHaveAttribute('data-replace', 'true');
  });

  test('Cubre rechazo: Redirige a "/" si el usuario NO es un administrador activo', () => {
    (useAuth as any).mockReturnValue({ 
      loading: false, 
      user: { uid: '123' }, 
      isAdminActive: false 
    });
    
    render(
      <ProtectedRoute>
        <div>Contenido Protegido</div>
      </ProtectedRoute>
    );
    
    expect(screen.getByTestId('mock-navigate')).toBeInTheDocument();
  });

  test('Cubre éxito: Renderiza el contenido si es un administrador activo', () => {
    (useAuth as any).mockReturnValue({ 
      loading: false, 
      user: { uid: '123' }, 
      isAdminActive: true 
    });
    
    render(
      <ProtectedRoute>
        <div data-testid="contenido-seguro">Panel de Control Confidencial</div>
      </ProtectedRoute>
    );
    
    expect(screen.getByTestId('contenido-seguro')).toBeInTheDocument();
    expect(screen.queryByTestId('mock-navigate')).not.toBeInTheDocument();
  });
});
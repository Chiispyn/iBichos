// src/pages/Principal/Principal.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import Principal from '../../../pages/Principal/principal';
import { useAuth } from '../../../context/authcontext';
import { usePrincipal } from '../../../pages/Principal/usePrincipal';
import { useNavigate } from 'react-router-dom';

// 1. MOCK DE DEPENDENCIAS
vi.mock('../../../context/authcontext', () => ({
  useAuth: vi.fn()
}));

vi.mock('../../../pages/Principal/usePrincipal', () => ({
  usePrincipal: vi.fn()
}));

// Mockeamos el enrutador de React para vigilar si las tarjetas nos envían a la URL correcta
vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn()
}));

describe('TC-42 — Módulo Principal (Dashboard de Inicio)', () => {
  const mockNavigate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (useNavigate as any).mockReturnValue(mockNavigate);
    
    // Simulamos la sesión del administrador
    (useAuth as any).mockReturnValue({ username: 'Felipe' });
  });

  test('Muestra el spinner cuando está cargando las métricas de hoy', () => {
    (usePrincipal as any).mockReturnValue({
      cargando: true,
      statsDia: { nuevosUsuarios: 0, capturasHoy: 0, pendientesHoy: 0 },
      fechaFormateada: 'sábado, 6 de junio de 2026'
    });

    render(<Principal />);
    
    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  test('Renderiza el saludo, la fecha y las métricas correctamente', () => {
    (usePrincipal as any).mockReturnValue({
      cargando: false,
      statsDia: { nuevosUsuarios: 12, capturasHoy: 45, pendientesHoy: 8 },
      fechaFormateada: 'sábado, 6 de junio de 2026'
    });

    render(<Principal />);
    
    // Verificamos el banner de bienvenida
    expect(screen.getByText(/¡Hola de nuevo,/)).toBeInTheDocument();
    expect(screen.getByText('Felipe')).toBeInTheDocument();
    expect(screen.getByText('sábado, 6 de junio de 2026')).toBeInTheDocument();

    // Verificamos que los números de las tarjetas se dibujen
    expect(screen.getByText('12')).toBeInTheDocument(); // Nuevos usuarios
    expect(screen.getByText('45')).toBeInTheDocument(); // Aprobadas hoy
    expect(screen.getByText('8')).toBeInTheDocument();  // Pendientes
  });

  test('La navegación mediante las tarjetas redirige a las rutas correctas', async () => {
    const user = userEvent.setup();
    (usePrincipal as any).mockReturnValue({
      cargando: false,
      statsDia: { nuevosUsuarios: 12, capturasHoy: 45, pendientesHoy: 8 },
      fechaFormateada: 'sábado, 6 de junio de 2026'
    });

    render(<Principal />);

    // Hacemos clic en la tarjeta de "Nuevos Usuarios"
    const cardUsuarios = screen.getByText('Gestionar usuarios');
    await user.click(cardUsuarios);
    expect(mockNavigate).toHaveBeenCalledWith('/usuarios');

    // Hacemos clic en la tarjeta de "Aprobadas Hoy"
    const cardCatalogo = screen.getByText('Ver galería de fotos');
    await user.click(cardCatalogo);
    expect(mockNavigate).toHaveBeenCalledWith('/catalogo');

    // Hacemos clic en la tarjeta de "Atención Requerida"
    const cardCapturas = screen.getByText('Moderar capturas dudosas');
    await user.click(cardCapturas);
    expect(mockNavigate).toHaveBeenCalledWith('/capturas');
  });
});
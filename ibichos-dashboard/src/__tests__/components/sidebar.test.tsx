import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Sidebar from '../../components/sidebar'; 
import { useAuth } from '../../context/authcontext';
import { signOut } from 'firebase/auth';

// 1. CORRECCIÓN: La ruta del mock ahora coincide exactamente con el import (../../) 
// y convertimos useAuth en un vi.fn()
vi.mock('../../context/authcontext', () => ({
  useAuth: vi.fn()
}));

// 2. CORRECCIÓN: Agregamos getAuth para que el archivo firebaseConfig.ts no explote
vi.mock('firebase/auth', () => ({
  getAuth: vi.fn(() => ({})),
  signOut: vi.fn(),
}));

// 3. CORRECCIÓN: Ajustamos la ruta a 2 niveles (../../) para que coincida con la estructura
vi.mock('../../config/firebaseConfig', () => ({
  auth: {},
}));

// Mock de Navegación manteniendo los componentes reales de React Router (como Link)
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Componente: Sidebar (Navegación)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Silenciamos los console.error para mantener limpia la terminal durante los tests de fallos
    vi.spyOn(console, 'error').mockImplementation(() => {}); 
  });

  test('Renderiza el componente y muestra el nombre del usuario', () => {
    (useAuth as any).mockReturnValue({ user: { uid: '123' }, username: 'Felipe' });
    
    // Usamos MemoryRouter porque el Sidebar utiliza <Link>
    render(<MemoryRouter><Sidebar /></MemoryRouter>);
    
    // El nombre aparece tanto en móvil como en escritorio
    const usernames = screen.getAllByText('Felipe');
    expect(usernames.length).toBeGreaterThan(0);
  });

  test('Cubre funcionalidad de Logout exitosa', async () => {
    const user = userEvent.setup();
    (useAuth as any).mockReturnValue({ user: { uid: '123' }, username: 'Felipe' });
    
    render(<MemoryRouter><Sidebar /></MemoryRouter>);
    
    // Clic en el botón de cerrar sesión (versión desktop)
    const logoutButtons = screen.getAllByRole('button', { name: /Cerrar Sesión/i });
    await user.click(logoutButtons[1]); // Hacemos clic en el segundo (desktop)
    
    expect(signOut).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  test('Cubre bloque catch si el Logout falla', async () => {
    const user = userEvent.setup();
    (useAuth as any).mockReturnValue({ user: { uid: '123' }, username: 'Felipe' });
    
    // Forzamos a Firebase a lanzar un error
    vi.mocked(signOut).mockRejectedValueOnce(new Error('Firebase network error'));
    
    render(<MemoryRouter><Sidebar /></MemoryRouter>);
    
    const logoutButtons = screen.getAllByRole('button', { name: /Cerrar Sesión/i });
    await user.click(logoutButtons[0]); // Hacemos clic en el primero (mobile)
    
    // Verificamos que el error se capturó en la consola
    expect(console.error).toHaveBeenCalledWith("Error al cerrar sesión", expect.any(Error));
  });

  test('Cubre Menú Móvil: Abre y cierra el colapsable', async () => {
    const user = userEvent.setup();
    (useAuth as any).mockReturnValue({ user: { uid: '123' }, username: 'Felipe' });
    
    render(<MemoryRouter><Sidebar /></MemoryRouter>);
    
    const hamburgerBtn = screen.getByLabelText('Toggle navigation');
    
    // Abrir menú (Cubre setIsNavOpen(!isNavOpen))
    await user.click(hamburgerBtn);
    expect(hamburgerBtn).toHaveAttribute('aria-expanded', 'true');
    
    // Hacer clic en un enlace móvil (Cubre la función closeNav)
    const mobileLink = screen.getAllByRole('link', { name: /Principal/i })[0];
    await user.click(mobileLink);
    
    // El menú debe haberse cerrado
    expect(hamburgerBtn).toHaveAttribute('aria-expanded', 'false');
  });

  test('Cubre clases activas de la ruta actual (Líneas getLinkClasses)', () => {
    (useAuth as any).mockReturnValue({ user: { uid: '123' }, username: 'Felipe' });
    
    // Inicializamos el router artificialmente en /analitica
    render(
      <MemoryRouter initialEntries={['/analitica']}>
        <Sidebar />
      </MemoryRouter>
    );
    
    // Los enlaces de Analítica deben tener la clase verde de activo
    const analiticaLinks = screen.getAllByRole('link', { name: /Analítica/i });
    expect(analiticaLinks[0]).toHaveClass('bg-success');
    
    // Los otros enlaces deben tener la clase hover
    const usuariosLinks = screen.getAllByRole('link', { name: /Usuarios/i });
    expect(usuariosLinks[0]).toHaveClass('hover-bg-opacity');
  });
});
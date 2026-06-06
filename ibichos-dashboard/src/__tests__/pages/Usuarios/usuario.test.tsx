// src/pages/Usuarios/Usuarios.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import Usuarios from '../../../pages/Usuarios/usuarios'; // Asegúrate de que coincida con tu nombre de archivo
import { useUsuarios } from '../../../pages/Usuarios/useUsuarios';

// 1. MOCK DEL HOOK
vi.mock('../../../pages/Usuarios/useUsuarios');

describe('TC-44 — Módulo de Gestión de Usuarios (UI)', () => {
  const mockSetFiltroTab = vi.fn();
  const mockSetBusqueda = vi.fn();
  const mockHandleToggleBan = vi.fn();

  // Objeto base para simular la respuesta del hook
  const baseMock = {
    cargando: false,
    adminsIds: ['user_admin_1'],
    modal: { isOpen: false, title: '', message: '', onConfirm: null, confirmColor: '' },
    closeModal: vi.fn(),
    filtroTab: 'Todos',
    setFiltroTab: mockSetFiltroTab,
    busqueda: '',
    setBusqueda: mockSetBusqueda,
    filtroNivel: 'Todos',
    setFiltroNivel: vi.fn(),
    ordenColumna: 'xp',
    ordenDireccion: 'desc',
    toggleOrden: vi.fn(),
    setPaginaActual: vi.fn(),
    totalPaginas: 1,
    paginaSegura: 1,
    indexInicio: 0,
    ITEMS_POR_PAGINA: 10,
    usuariosProcesados: [],
    usuariosPaginados: [
      { id: 'user_admin_1', username: 'FelipeAdmin', email: 'felipe@admin.cl', level: 'Entomólogo', xp: 5000, isShadowBanned: false, createdAt: '2026-01-01', strikes: 0 },
      { id: 'user_normal_2', username: 'JuanExplorador', email: 'juan@test.cl', level: 'Casual', xp: 120, isShadowBanned: false, createdAt: '2026-06-01', strikes: 1 }
    ],
    handleHacerAdmin: vi.fn(),
    handleQuitarAdmin: vi.fn(),
    handleToggleBan: mockHandleToggleBan
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Muestra el spinner de carga si el hook indica que está sincronizando', () => {
    (useUsuarios as any).mockReturnValue({ ...baseMock, cargando: true });
    render(<Usuarios />);
    
    expect(screen.getByText('Sincronizando base de datos...')).toBeInTheDocument();
  });

  test('Renderiza la tabla de usuarios con las insignias correctas (Admin)', () => {
    (useUsuarios as any).mockReturnValue(baseMock);
    render(<Usuarios />);
    
    // Verificamos que se muestren ambos usuarios
    expect(screen.getByText('FelipeAdmin')).toBeInTheDocument();
    expect(screen.getByText('JuanExplorador')).toBeInTheDocument();
    
    // FelipeAdmin debe tener la insignia de Admin (está en adminsIds)
    const adminBadges = screen.getAllByText('Admin');
    expect(adminBadges.length).toBe(1);
  });

  test('Muestra estado vacío si la búsqueda no arroja resultados', () => {
    (useUsuarios as any).mockReturnValue({
      ...baseMock,
      usuariosPaginados: [] // Arreglo vacío
    });
    render(<Usuarios />);
    
    expect(screen.getByText('No se encontraron resultados')).toBeInTheDocument();
    expect(screen.getByText('Prueba cambiando los filtros o la búsqueda.')).toBeInTheDocument();
  });

  test('Permite cambiar de pestaña de filtro', async () => {
    const user = userEvent.setup();
    (useUsuarios as any).mockReturnValue(baseMock);
    render(<Usuarios />);
    
    // Hacemos clic en la pestaña "Baneados"
    const tabBaneados = screen.getByRole('button', { name: 'Baneados' });
    await user.click(tabBaneados);
    
    expect(mockSetFiltroTab).toHaveBeenCalledWith('Baneados');
  });

  test('La interacción con el input de búsqueda notifica al hook', async () => {
    const user = userEvent.setup();
    (useUsuarios as any).mockReturnValue(baseMock);
    render(<Usuarios />);
    
    const searchInput = screen.getByPlaceholderText('Buscar nombre o correo...');
    
    // Simulamos que el usuario escribe "juan"
    await user.type(searchInput, 'juan');
    
    // Debe haberse llamado setBusqueda (podría ser llamado varias veces por cada letra)
    expect(mockSetBusqueda).toHaveBeenCalled();
  });

  test('El botón de banear invoca la función de moderación del hook', async () => {
    const user = userEvent.setup();
    (useUsuarios as any).mockReturnValue(baseMock);
    render(<Usuarios />);
    
    // Buscamos los botones por su título ("Aplicar Shadowban")
    // Hay 2 usuarios que NO están baneados, así que habrá 2 botones
    const banButtons = screen.getAllByTitle('Aplicar Shadowban');
    
    // Hacemos clic en el botón del segundo usuario (JuanExplorador)
    await user.click(banButtons[1]);
    
    // Verificamos que se llamó a la función pasándole el objeto usuario correspondiente
    expect(mockHandleToggleBan).toHaveBeenCalledTimes(1);
    expect(mockHandleToggleBan).toHaveBeenCalledWith(
      expect.objectContaining({ username: 'JuanExplorador' })
    );
  });

  test('Muestra el modal de confirmación si el estado isOpen es true', () => {
    (useUsuarios as any).mockReturnValue({
      ...baseMock,
      modal: {
        isOpen: true,
        title: 'Shadowbanear Usuario',
        message: '¿Deseas bloquear a JuanExplorador?',
        onConfirm: vi.fn(),
        confirmColor: 'btn-dark'
      }
    });
    render(<Usuarios />);
    
    expect(screen.getByText('Shadowbanear Usuario')).toBeInTheDocument();
    expect(screen.getByText('¿Deseas bloquear a JuanExplorador?')).toBeInTheDocument();
  });
});
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import Capturas from '../../../pages/Capturas/capturas';
import { useCapturas } from '../../../pages/Capturas/useCapturas';

vi.mock('../../../pages/Capturas/useCapturas');

describe('TC-56 — Módulo de Capturas (Interfaz y UI)', () => {
  const mockSetFiltro = vi.fn();
  const mockSetShowModal = vi.fn();
  const mockSetSelectedImg = vi.fn();
  const mockHandleModeracion = vi.fn();
  const mockHandleEliminar = vi.fn();
  const mockOpenModal = vi.fn();

  const baseMock = {
    capturas: [],
    capturasFiltradas: [],
    filtro: 'PENDING_REVIEW',
    setFiltro: mockSetFiltro,
    cargando: false,
    showModal: false,
    setShowModal: mockSetShowModal,
    selectedImg: null,
    setSelectedImg: mockSetSelectedImg,
    userMap: { 'user_felipe': { name: 'Felipe', email: 'felipe@test.cl' } },
    handleModeracion: mockHandleModeracion,
    handleEliminar: mockHandleEliminar,
    openModal: mockOpenModal,
    getDangerColor: () => 'danger',
    traducirPeligro: () => 'Venenoso'
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ================================================================
  // 1. ESTADOS DE CARGA Y VACÍO
  // ================================================================
  test('Flujo 1: Maneja los estados iniciales (Spinner y Sin Datos)', () => {
    // 1A. Probamos spinner
    (useCapturas as any).mockReturnValue({ ...baseMock, cargando: true });
    const { unmount } = render(<Capturas />);
    expect(screen.getByRole('status')).toBeInTheDocument();
    
    // Desmontamos para probar el siguiente estado limpiamente
    unmount();

    // 1B. Probamos estado vacío
    (useCapturas as any).mockReturnValue(baseMock);
    render(<Capturas />);
    expect(screen.getByText('No hay capturas que mostrar aquí.')).toBeInTheDocument();
  });

  // ================================================================
  // 2. RENDERIZADO VISUAL
  // ================================================================
  test('Flujo 2: Renderiza la Grilla, variaciones de Badges y Alertas de Fraude', () => {
    const capturasMocks = [
      { id: 'cap1', userId: 'user_felipe', lat: -36.82, lng: -73.04, status: 'PENDING_REVIEW', insectName: 'Araña', category: 'ARACHNID', confidence: 0.95 },
      { id: 'cap2', userId: 'user_felipe', lat: -36.82, lng: -73.04, status: 'APPROVED', insectName: 'Abeja', category: 'HYMENOPTERA', confidence: 0.8 },
      { id: 'cap3', userId: 'user_felipe', lat: -36.82, lng: -73.04, status: 'REJECTED', insectName: 'Mosca', category: 'OTHER', confidence: 0.2 }
    ];

    (useCapturas as any).mockReturnValue({
      ...baseMock,
      capturas: capturasMocks,
      capturasFiltradas: capturasMocks // Forzamos a mostrarlas todas
    });

    render(<Capturas />);
    
    expect(screen.getByText('Araña')).toBeInTheDocument();
    
    // Validamos que los ternarios de los Badges se pintan (líneas 84-101 aprox)
    expect(screen.getByText('Revisión')).toBeInTheDocument(); // Este solo existe una vez, así que getByText funciona
    // Usamos getAllByText porque existen tanto en el Badge como en el Botón desactivado
    expect(screen.getAllByText('Aprobada').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Rechazada').length).toBeGreaterThan(0);
    
    // Validamos cálculo de fraude
    expect(screen.getAllByText(/POSIBLE FRAUDE/i).length).toBeGreaterThan(0);
  });

  // ================================================================
  // 3. EVENTOS DE UI (BOTONES Y SELECTS)
  // ================================================================
  test('Flujo 3: Permite interactuar con Filtros, Botones de Moderación y Categorías', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      filtro: 'REJECTED', // Empezamos en rechazadas para poder volver a pendientes
      capturasFiltradas: [{ id: 'cap1', userId: 'user_felipe', status: 'PENDING_REVIEW', category: 'OTHER', confidence: 0.8 }]
    });

    render(<Capturas />);

    // Filtros Superiores
    await user.click(screen.getByRole('button', { name: /Pendientes de Revisión/i }));
    expect(mockSetFiltro).toHaveBeenCalledWith('PENDING_REVIEW');
    
    await user.click(screen.getByRole('button', { name: /Rechazadas \/ Ocultas/i }));
    expect(mockSetFiltro).toHaveBeenCalledWith('REJECTED');

    // Botones de acción directa
    await user.click(screen.getByRole('button', { name: 'Aprobar' }));
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'APPROVED');
    
    await user.click(screen.getByRole('button', { name: /Reportar Fraude/i }));
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'REJECTED', undefined, true);

    // Modificar Categoría
    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'COLEOPTERA');
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'PENDING_REVIEW', 'COLEOPTERA');
  });

  // ================================================================
  // 4. MODALES
  // ================================================================
  test('Flujo 4: Gestiona correctamente la apertura y cierre de Modales', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      showModal: true, // Ambos abiertos por defecto
      selectedImg: 'foto.jpg',
      capturasFiltradas: [{ id: 'cap1', userId: 'user_felipe', status: 'PENDING_REVIEW', confidence: 0.8 }]
    });

    render(<Capturas />);

    // Verificamos presencia en DOM
    expect(screen.getByAltText('Zoom de insecto')).toBeInTheDocument();
    expect(screen.getByText('¿Ocultar captura?')).toBeInTheDocument();

    // Clics oscuros en el DOM (Botones X y Overlays de fondo)
    const closeButtons = document.querySelectorAll('.btn-close');
    if (closeButtons.length > 0) await user.click(closeButtons[0]);
    expect(mockSetSelectedImg).toHaveBeenCalledWith(null);

    const modalContainers = document.querySelectorAll('.modal.d-block');
    if (modalContainers.length > 1) await user.click(modalContainers[1]);
    expect(mockSetShowModal).toHaveBeenCalledWith(false);

    // Clics directos en botones del modal de confirmación
    await user.click(screen.getByRole('button', { name: 'Cancelar' }));
    expect(mockSetShowModal).toHaveBeenCalledWith(false);

    await user.click(screen.getByRole('button', { name: 'Sí, Ocultar' }));
    expect(mockHandleEliminar).toHaveBeenCalled();

    // Clic en la papelera de la grilla 
    const openModalButton = document.querySelector('.btn-dark.bg-opacity-50');
    if(openModalButton) await user.click(openModalButton);
    expect(mockOpenModal).toHaveBeenCalledWith('cap1');
  });
});
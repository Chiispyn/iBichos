// src/__tests__/pages/Capturas/capturas.test.tsx
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

  test('Muestra el spinner de carga inicial', () => {
    (useCapturas as any).mockReturnValue({ ...baseMock, cargando: true });
    render(<Capturas />);
    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  test('Muestra estado vacío cuando no hay capturas', () => {
    (useCapturas as any).mockReturnValue(baseMock);
    render(<Capturas />);
    expect(screen.getByText('No hay capturas que mostrar aquí.')).toBeInTheDocument();
  });

  test('Renderiza la grilla y detecta "Posible Fraude"', () => {
    const capturaBase = {
      userId: 'user_felipe', lat: -36.82, lng: -73.04, status: 'PENDING_REVIEW',
      insectName: 'Araña de Rincón', category: 'ARACHNID', confidence: 0.95
    };
    const mockCapturasArray = [
      { id: 'cap1', ...capturaBase },
      { id: 'cap2', ...capturaBase },
      { id: 'cap3', ...capturaBase } 
    ];

    (useCapturas as any).mockReturnValue({
      ...baseMock,
      capturas: mockCapturasArray,
      capturasFiltradas: [mockCapturasArray[0]] 
    });

    render(<Capturas />);
    expect(screen.getByText('Araña de Rincón')).toBeInTheDocument();
    expect(screen.getByText(/POSIBLE FRAUDE/i)).toBeInTheDocument();
  });

  test('Alterna entre pestañas de PENDIENTES y RECHAZADAS', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue(baseMock);
    render(<Capturas />);
    await user.click(screen.getByRole('button', { name: /Rechazadas \/ Ocultas/i }));
    expect(mockSetFiltro).toHaveBeenCalledWith('REJECTED');
  });

  test('Dispara acciones de moderación (Aprobar, Rechazar, Strike)', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      capturasFiltradas: [{ id: 'cap1', userId: 'user_felipe', status: 'PENDING_REVIEW', confidence: 0.8 }]
    });

    render(<Capturas />);
    await user.click(screen.getByRole('button', { name: 'Aprobar' }));
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'APPROVED');

    await user.click(screen.getByRole('button', { name: /Reportar Fraude/i }));
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'REJECTED', undefined, true);
  });

  // 👇 TEST NUEVO: Cubre el selector de categorías
  test('Permite cambiar la categoría desde el select', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      capturasFiltradas: [{ id: 'cap1', userId: 'user_felipe', status: 'PENDING_REVIEW', category: 'OTHER', confidence: 0.8 }]
    });

    render(<Capturas />);
    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'COLEOPTERA'); // Seleccionamos Escarabajo
    expect(mockHandleModeracion).toHaveBeenCalledWith('cap1', 'PENDING_REVIEW', 'COLEOPTERA');
  });

  // 👇 TEST NUEVO: Cubre el Modal de Zoom (Líneas 207-227)
  test('Renderiza el modal de zoom de imagen', () => {
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      selectedImg: 'https://bicho.cl/foto.jpg' // Forzamos que haya una imagen seleccionada
    });
    render(<Capturas />);
    // Verificamos que se renderizó el alt text de la imagen ampliada
    expect(screen.getByAltText('Zoom de insecto')).toBeInTheDocument();
  });

  // 👇 TEST NUEVO: Cubre el Modal de Eliminar (Líneas 228-252)
  test('Renderiza el modal de confirmación y permite cancelar o eliminar', async () => {
    const user = userEvent.setup();
    (useCapturas as any).mockReturnValue({
      ...baseMock,
      showModal: true // Forzamos a que el modal esté abierto
    });
    render(<Capturas />);
    
    // Verificamos que el modal existe
    expect(screen.getByText('¿Ocultar captura?')).toBeInTheDocument();

    // Hacemos clic en Cancelar
    await user.click(screen.getByRole('button', { name: 'Cancelar' }));
    expect(mockSetShowModal).toHaveBeenCalledWith(false);

    // Hacemos clic en Eliminar
    await user.click(screen.getByRole('button', { name: 'Sí, Ocultar' }));
    expect(mockHandleEliminar).toHaveBeenCalled();
  });
});
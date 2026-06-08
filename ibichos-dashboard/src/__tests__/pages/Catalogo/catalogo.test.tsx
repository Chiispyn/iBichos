// src/__tests__/pages/Catalogo/catalogo.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import Catalogo from '../../../pages/Catalogo/catalogo';
import { useCatalogo } from '../../../pages/Catalogo/useCatalogo';

// 1. MOCK DEL HOOK (Aislamos la vista)
vi.mock('../../../pages/Catalogo/useCatalogo');

describe('TC-45 — Módulo de Catálogo (Interfaz y Moderación UI)', () => {
  const mockHandleUpdate = vi.fn();
  const mockOpenDeleteModal = vi.fn();
  const mockHandleReject = vi.fn();
  const mockSetBusqueda = vi.fn();
  const mockSetSelectedImg = vi.fn();

  const baseMock = {
    cargando: false,
    busqueda: '',
    setBusqueda: mockSetBusqueda,
    selectedImg: null,
    setSelectedImg: mockSetSelectedImg,
    showModal: false,
    setShowModal: vi.fn(),
    userMap: { 'user1': { name: 'Juan', email: 'juan@test.cl' } },
    filtered: [
      { 
        id: 'cap_123', 
        insectName: 'Araña Falsa', 
        scientificName: 'Falsus spiderus', 
        category: 'OTHER', 
        dangerLevel: 'UNKNOWN', 
        confidence: 0.85, 
        userId: 'user1', 
        imageUrl: 'https://img.test/1.jpg' 
      }
    ],
    handleUpdate: mockHandleUpdate,
    handleReject: mockHandleReject,
    openDeleteModal: mockOpenDeleteModal,
    getDangerBadge: () => 'bg-secondary'
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Muestra spinner de carga inicial', () => {
    (useCatalogo as any).mockReturnValue({ ...baseMock, cargando: true });
    render(<Catalogo />);
    expect(screen.getByText('Cargando catálogo...')).toBeInTheDocument();
  });

  test('Muestra estado vacío si la búsqueda no encuentra especies', () => {
    (useCatalogo as any).mockReturnValue({ ...baseMock, filtered: [] });
    render(<Catalogo />);
    expect(screen.getByText('No se encontraron especies con ese criterio.')).toBeInTheDocument();
  });

  test('Renderiza las capturas aprobadas en la galería', () => {
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    expect(screen.getByText('Araña Falsa')).toBeInTheDocument();
    expect(screen.getByText('Falsus spiderus')).toBeInTheDocument();
    expect(screen.getByText('Juan')).toBeInTheDocument(); 
  });

  test('La barra de búsqueda actualiza el estado del hook', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    const searchInput = screen.getByPlaceholderText(/Buscar por insecto, colector o correo/i);
    await user.type(searchInput, 'Araña');

    expect(mockSetBusqueda).toHaveBeenCalled();
  });

  test('Permite cambiar la categoría biológica desde el select', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    const selects = screen.getAllByRole('combobox');
    const categorySelect = selects[0]; 

    await user.selectOptions(categorySelect, 'ARACHNID');
    expect(mockHandleUpdate).toHaveBeenCalledWith('cap_123', { category: 'ARACHNID' });
  });

  test('Permite cambiar el nivel de peligrosidad desde el select', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    const selects = screen.getAllByRole('combobox');
    const dangerSelect = selects[1];

    await user.selectOptions(dangerSelect, 'VENOMOUS');
    expect(mockHandleUpdate).toHaveBeenCalledWith('cap_123', { dangerLevel: 'VENOMOUS' });
  });

  test('Abre el modal al intentar quitar una captura del catálogo', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    const deleteBtn = screen.getByTitle('Rechazar y quitar del catálogo');
    await user.click(deleteBtn);

    expect(mockOpenDeleteModal).toHaveBeenCalledWith('cap_123');
  });

  test('Confirma el rechazo dentro del modal de seguridad', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue({ ...baseMock, showModal: true });
    render(<Catalogo />);

    expect(screen.getByText('¿Mover a rechazadas?')).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: 'Sí, Quitar' });
    await user.click(confirmBtn);

    expect(mockHandleReject).toHaveBeenCalledTimes(1);
  });
});
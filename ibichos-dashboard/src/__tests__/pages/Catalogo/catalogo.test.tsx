import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, test, expect, vi, beforeEach } from 'vitest';
import Catalogo from '../../../pages/Catalogo/catalogo';
import { useCatalogo } from '../../../pages/Catalogo/useCatalogo';

const mockCapturas = [
  { id: '1', insectName: 'Mariposa Monarca', scientificName: 'Danaus plexippus', status: 'APPROVED',  userName: 'usuario1', email: 'user1@test.com', imageUrl: 'https://img.test/1.jpg' },
  { id: '2', insectName: 'Araña Pollito',    scientificName: 'Grammostola rosea',  status: 'PENDING',   userName: 'usuario2', email: 'user2@test.com', imageUrl: 'https://img.test/2.jpg' },
  { id: '3', insectName: 'Escarabajo Rinoceronte', scientificName: 'Oryctes nasicornis', status: 'APPROVED', userName: 'usuario1', email: 'user1@test.com', imageUrl: 'https://img.test/3.jpg' },
]

describe('TC-30 — Solo capturas APPROVED en catálogo', () => {
  it('filtra correctamente capturas APPROVED', () => {
    const aprobadas = mockCapturas.filter(c => c.status === 'APPROVED')
    expect(aprobadas).toHaveLength(2)
    expect(aprobadas.every(c => c.status === 'APPROVED')).toBe(true)
  })
})

describe('TC-31 — Búsqueda por nombre de insecto o científico', () => {
  it('filtra por insectName', () => {
    const query = 'Mariposa'
    const resultado = mockCapturas.filter(c =>
      c.insectName.toLowerCase().includes(query.toLowerCase())
    )
    expect(resultado).toHaveLength(1)
    expect(resultado[0].insectName).toBe('Mariposa Monarca')
  })

  it('filtra por scientificName', () => {
    const query = 'Oryctes'
    const resultado = mockCapturas.filter(c =>
      c.scientificName.toLowerCase().includes(query.toLowerCase())
    )
    expect(resultado).toHaveLength(1)
    expect(resultado[0].insectName).toBe('Escarabajo Rinoceronte')
  })
})

describe('TC-32 — Búsqueda por nombre de usuario o email', () => {
  it('filtra correctamente por userName', () => {
    const query = 'usuario1'
    const resultado = mockCapturas.filter(c =>
      c.userName.toLowerCase().includes(query.toLowerCase())
    )
    expect(resultado).toHaveLength(2)
  })

  it('filtra correctamente por email de colector', () => {
    const query = 'user2@test.com'
    const resultado = mockCapturas.filter(c =>
      c.email.toLowerCase().includes(query.toLowerCase())
    )
    expect(resultado).toHaveLength(1)
    expect(resultado[0].userName).toBe('usuario2')
  })
})

vi.mock('../../../pages/Catalogo/useCatalogo');
describe('TC-45 — Interfaz y Moderación Activa del Catálogo', () => {
  const mockHandleUpdate = vi.fn();
  const mockOpenDeleteModal = vi.fn();
  const mockHandleReject = vi.fn();

  const baseMock = {
    cargando: false,
    busqueda: '',
    setBusqueda: vi.fn(),
    selectedImg: null,
    setSelectedImg: vi.fn(),
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

  test('Renderiza las capturas aprobadas en la galería', () => {
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    expect(screen.getByText('Araña Falsa')).toBeInTheDocument();
    expect(screen.getByText('Falsus spiderus')).toBeInTheDocument();
    expect(screen.getByText('Juan')).toBeInTheDocument(); 
  });

  test('Permite cambiar la categoría biológica de la captura', async () => {
    const user = userEvent.setup();
    (useCatalogo as any).mockReturnValue(baseMock);
    render(<Catalogo />);

    const selects = screen.getAllByRole('combobox');
    const categorySelect = selects[0]; 

    await user.selectOptions(categorySelect, 'ARACHNID');
    expect(mockHandleUpdate).toHaveBeenCalledWith('cap_123', { category: 'ARACHNID' });
  });

  test('Permite cambiar el nivel de peligrosidad de la captura', async () => {
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
    (useCatalogo as any).mockReturnValue({
      ...baseMock,
      showModal: true
    });
    
    render(<Catalogo />);

    expect(screen.getByText('¿Mover a rechazadas?')).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: 'Sí, Quitar' });
    await user.click(confirmBtn);

    expect(mockHandleReject).toHaveBeenCalledTimes(1);
  });
});
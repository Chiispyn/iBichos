// src/pages/Analitica/Analitica.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import Analitica from '../../../pages/Analitica/analitica';
import { useAnalitica } from '../../../pages/Analitica/useAnalitica';

// 1. MOCK DEL CEREBRO (HOOK)
// Le decimos a Vitest que suplante el hook real por uno falso que nosotros controlamos
vi.mock('./useAnalitica');

// Mockeamos Recharts porque jsdom (el navegador invisible) no sabe dibujar gráficos SVG
vi.mock('recharts', async () => {
  const OriginalRecharts = await vi.importActual('recharts');
  return {
    ...OriginalRecharts,
    ResponsiveContainer: ({ children }: any) => <div data-testid="recharts-container">{children}</div>,
  };
});

describe('TC-46 — Módulo de Analítica (Interfaz y Eventos UI)', () => {
  const mockHandleExportCSV = vi.fn();
  const mockHandleSaveSnapshot = vi.fn();
  const mockSetActiveTab = vi.fn();
  const mockSetSelectedRegion = vi.fn();

  // 2. ESTADO INICIAL SIMULADO
  // Esto es lo que el hook "devolvería" si ya hubiera procesado todo
  const baseMock = {
    activeTab: 'actividad',
    setActiveTab: mockSetActiveTab,
    loading: false,
    showSuccessModal: false,
    setShowSuccessModal: vi.fn(),
    stats: { totalUsers: 150, totalSessions: 300, totalMinutes: 4500, totalCaptures: 1200, pendingReview: 15 },
    levelData: [], categoryData: [], dangerData: [], sessionsData: [], 
    retentionStats: { day1: 40, day7: 20, day30: 5 },
    activationStats: { activados: 80, abandono: 20 },
    validationData: [], shadowbanData: [], aiConfidenceData: [], medalsData: [], genreData: [], ageData: [],
    regionsList: ['Metropolitana', 'Valparaíso', 'Biobío'],
    selectedRegion: 'Todas',
    setSelectedRegion: mockSetSelectedRegion,
    handleExportCSV: mockHandleExportCSV,
    handleSaveSnapshot: mockHandleSaveSnapshot
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // --- TESTS DE RENDERIZADO VISUAL ---

  test('Muestra el spinner de carga inicial', () => {
    (useAnalitica as any).mockReturnValue({ loading: true });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    
    expect(screen.getByText('Analizando datos...')).toBeInTheDocument();
  });

  test('Renderiza el panel principal con las métricas de Actividad', () => {
    (useAnalitica as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    
    expect(screen.getByText('Panel de Inteligencia iBichos')).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument(); // totalUsers del mock
    expect(screen.getByText('Exploradores Registrados')).toBeInTheDocument();
  });

  // --- TESTS DE INTERACCIÓN DEL USUARIO ---

  test('El usuario puede navegar entre las diferentes pestañas', async () => {
    const user = userEvent.setup();
    (useAnalitica as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    
    // El administrador hace clic en la pestaña "Demografía"
    const tabDemografia = screen.getByRole('button', { name: /Demografía/i });
    await user.click(tabDemografia);

    // El componente le avisa al hook que cambie el estado
    expect(mockSetActiveTab).toHaveBeenCalledWith('demografia');
  });

  test('El administrador puede cambiar el filtro regional', async () => {
    const user = userEvent.setup();
    
    // Forzamos la vista de Demografía para que aparezca el select de regiones
    (useAnalitica as any).mockReturnValue({
      ...baseMock,
      activeTab: 'demografia'
    });
    
    render(<BrowserRouter><Analitica /></BrowserRouter>);

    // Buscamos el combobox (select)
    const selectRegion = screen.getByRole('combobox');
    
    // Simulamos que el admin selecciona "Biobío"
    await user.selectOptions(selectRegion, 'Biobío');
    
    // Verificamos que se dispare la función de actualización
    expect(mockSetSelectedRegion).toHaveBeenCalledWith('Biobío');
  });

  test('Los botones de exportar y guardar disparan las funciones del hook', async () => {
    const user = userEvent.setup();
    (useAnalitica as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Analitica /></BrowserRouter>);

    // Clic en Guardar en la Nube
    const btnGuardar = screen.getByRole('button', { name: /Guardar en la Nube/i });
    await user.click(btnGuardar);
    expect(mockHandleSaveSnapshot).toHaveBeenCalledWith(false);

    // Clic en Exportar CSV
    const btnExportar = screen.getByRole('button', { name: /Exportar CSV/i });
    await user.click(btnExportar);
    expect(mockHandleExportCSV).toHaveBeenCalledTimes(1);
  });

  // --- TESTS DE MODALES ---

  test('Muestra el modal de confirmación al guardar exitosamente', () => {
    // Forzamos el modal abierto en el estado
    (useAnalitica as any).mockReturnValue({
      ...baseMock,
      showSuccessModal: true
    });

    render(<BrowserRouter><Analitica /></BrowserRouter>);
    
    expect(screen.getByText('¡Reporte Guardado!')).toBeInTheDocument();
    expect(screen.getByText('Instantánea exitosa')).toBeInTheDocument();
  });
});
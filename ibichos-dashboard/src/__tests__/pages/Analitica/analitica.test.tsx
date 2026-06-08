// src/__tests__/pages/Analitica/analitica.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';

import Analitica from '../../../pages/Analitica/analitica';
import { useAnalitica } from '../../../pages/Analitica/useAnalitica';

// 1. MOCK DE ROUTER
const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
  BrowserRouter: ({ children }: any) => <div>{children}</div>
}));

// 2. MOCK DEL HOOK
vi.mock('../../../pages/Analitica/useAnalitica');

// 3. MOCK DE RECHARTS
vi.mock('recharts', async () => {
  const OriginalRecharts = await vi.importActual('recharts');
  return {
    ...OriginalRecharts,
    ResponsiveContainer: ({ children }: any) => <div data-testid="recharts-container">{children}</div>,
    BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
    LineChart: ({ children }: any) => <div data-testid="line-chart">{children}</div>,
    PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
    AreaChart: ({ children }: any) => <div data-testid="area-chart">{children}</div>,
  };
});

describe('TC-65 — Módulo de Analítica (Interfaz, Pestañas y Eventos)', () => {
  // Datos infalibles para llenar todas las gráficas de tu componente
  const baseMock = {
    loading: false,
    activeTab: 'actividad',
    setActiveTab: vi.fn(),
    showSuccessModal: false,
    setShowSuccessModal: vi.fn(),
    
    // Tarjetas principales
    stats: { totalUsers: 150, totalCaptures: 300, pendingReview: 20, totalSessions: 50, totalMinutes: 1200 },
    retentionStats: { day1: '50.0', day7: '25.0', day30: '10.0' },
    activationStats: { activados: 120, abandono: 30 },
    
    // Pestaña: Actividad
    levelData: [{ name: 'Casual', value: 10 }],
    sessionsData: [{ name: '01 Jun', minutos: 20 }],
    medalsData: [{ name: 'Cazador', value: 5 }],
    
    // Pestaña: Biodiversidad
    categoryData: [{ name: 'Arácnidos', value: 40 }],
    dangerData: [{ name: 'Venenoso', value: 5 }],
    
    // Pestaña: Demografía
    regionsList: ['Todas', 'Biobío'],
    selectedRegion: 'Todas',
    setSelectedRegion: vi.fn(),
    genreData: [{ name: 'Masculino', value: 10 }],
    ageData: [{ name: '18-25', value: 10 }],
    
    // Pestaña: Moderación
    validationData: [
      { name: 'Aprobadas', value: 10 },
      { name: 'Rechazadas (IA)', value: 5 },
      { name: 'Rechazadas (Admin)', value: 2 },
      { name: 'Pendientes', value: 3 }
    ],
    shadowbanData: [{ name: 'Sanos', value: 100 }],
    aiConfidenceData: [{ name: 'Arácnidos', value: 80 }],
    
    // Funciones
    handleExportCSV: vi.fn(),
    handleSaveSnapshot: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Muestra el spinner de carga (Líneas iniciales)', () => {
    (useAnalitica as any).mockReturnValue({ ...baseMock, loading: true });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  // 👇 AQUÍ ESTÁ EL TRUCO: Forzamos la apertura de cada pestaña individualmente

  test('Renderiza Pestaña 1: ACTIVIDAD completa', () => {
    (useAnalitica as any).mockReturnValue({ ...baseMock, activeTab: 'actividad' });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    // Si aparece este texto, pasó por las líneas 172-367
    expect(screen.getByText(/Distribución por Ligas/i)).toBeInTheDocument();
    expect(screen.getAllByTestId('recharts-container').length).toBeGreaterThan(0);
  });

  test('Renderiza Pestaña 2: BIODIVERSIDAD completa', () => {
    (useAnalitica as any).mockReturnValue({ ...baseMock, activeTab: 'biodiversidad' });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    // Si aparece este texto, pasó por las líneas 367-517
    expect(screen.getByText(/Riesgos para la Salud/i)).toBeInTheDocument();
    expect(screen.getAllByTestId('recharts-container').length).toBeGreaterThan(0);
  });

  test('Renderiza Pestaña 3: DEMOGRAFÍA completa', () => {
    (useAnalitica as any).mockReturnValue({ ...baseMock, activeTab: 'demografia' });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    // Si aparece este texto, pasó por las líneas 517-647
    expect(screen.getByText(/Filtrar Análisis por Región/i)).toBeInTheDocument();
    expect(screen.getAllByTestId('recharts-container').length).toBeGreaterThan(0);
  });

  test('Renderiza Pestaña 4: MODERACIÓN completa', () => {
    (useAnalitica as any).mockReturnValue({ ...baseMock, activeTab: 'moderacion' });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    // Si aparece este texto, pasó por las líneas 647-785
    expect(screen.getByText(/Precisión del Motor de IA/i)).toBeInTheDocument();
    expect(screen.getAllByTestId('recharts-container').length).toBeGreaterThan(0);
  });

  // 👇 Probamos los casos sin datos
  test('Renderiza mensajes de "Sin datos" en medallas, género y edad', () => {
    (useAnalitica as any).mockReturnValue({ 
      ...baseMock, 
      activeTab: 'demografia', // Probamos con demografía abierta
      genreData: [], 
      ageData: [] 
    });
    render(<BrowserRouter><Analitica /></BrowserRouter>);
    expect(screen.getAllByText(/Sin datos en esta zona/i).length).toBeGreaterThan(0);
  });

  // 👇 Probamos los botones, la navegación y el Modal (Líneas 70, 785-801)
  test('Interactúa con botones, navegación y Modal', async () => {
    const user = userEvent.setup();
    (useAnalitica as any).mockReturnValue({ ...baseMock, showSuccessModal: true });
    render(<BrowserRouter><Analitica /></BrowserRouter>);

    // Clic en Guardar Nube y Exportar
    await user.click(screen.getByRole('button', { name: /Guardar en la Nube/i }));
    expect(baseMock.handleSaveSnapshot).toHaveBeenCalled();

    await user.click(screen.getByRole('button', { name: /Exportar CSV/i }));
    expect(baseMock.handleExportCSV).toHaveBeenCalled();

    // Verificamos que el modal de éxito (línea 791) se dibuja
    expect(screen.getByText(/¡Reporte Guardado!/i)).toBeInTheDocument();
    
    // Cerramos el modal
    await user.click(screen.getByRole('button', { name: /Aceptar/i }));
    expect(baseMock.setShowSuccessModal).toHaveBeenCalledWith(false);
  });
});
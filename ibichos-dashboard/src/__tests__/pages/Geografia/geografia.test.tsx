// src/__tests__/pages/Geografia/geografia.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';

import Geografia from '../../../pages/Geografia/geografia';
import { useGeografia } from '../../../pages/Geografia/useGeografia';

// 1. MOCK DE DEPENDENCIAS
vi.mock('../../../pages/Geografia/useGeografia');

// Mockeamos Recharts
vi.mock('recharts', async () => {
  const OriginalRecharts = await vi.importActual('recharts');
  return {
    ...OriginalRecharts,
    ResponsiveContainer: ({ children }: any) => <div data-testid="recharts-container">{children}</div>,
  };
});

describe('TC-43 — Módulo de Geografía y Distribución', () => {
  const mockSetTopComunasLimit = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    
    (useGeografia as any).mockReturnValue({
      loading: false,
      regionData: [{ name: 'Metropolitana', value: 100 }, { name: 'Valparaíso', value: 50 }],
      comunaData: [
        { name: 'Santiago', value: 40 }, { name: 'Providencia', value: 30 },
        { name: 'Maipú', value: 20 }, { name: 'Las Condes', value: 10 }
      ],
      topComunasLimit: 5,
      setTopComunasLimit: mockSetTopComunasLimit,
      stats: { topRegion: 'Metropolitana', topComuna: 'Santiago', totalLocs: 4 }
    });
  });

  test('Muestra spinner de carga si loading es true', () => {
    (useGeografia as any).mockReturnValue({ loading: true });
    render(<BrowserRouter><Geografia /></BrowserRouter>);
    expect(screen.getByText('Analizando territorio...')).toBeInTheDocument();
  });

  test('Renderiza las tarjetas de resumen superior (Top Región, Top Comuna, Total Zonas)', () => {
    render(<BrowserRouter><Geografia /></BrowserRouter>);
    
    // Validamos que los datos del mock se muestren en las tarjetas
    expect(screen.getByText('Metropolitana')).toBeInTheDocument();
    expect(screen.getByText('Santiago')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument(); // Total de zonas
  });

  test('Permite alternar entre el Top 5 y Top 10 de comunas', async () => {
    const user = userEvent.setup();
    render(<BrowserRouter><Geografia /></BrowserRouter>);

    // Buscamos los botones por su texto exacto
    const btnTop5 = screen.getByRole('button', { name: 'Top 5' });
    const btnTop10 = screen.getByRole('button', { name: 'Top 10' });

    // Hacemos clic en Top 10
    await user.click(btnTop10);
    expect(mockSetTopComunasLimit).toHaveBeenCalledWith(10);

    // Hacemos clic en Top 5
    await user.click(btnTop5);
    expect(mockSetTopComunasLimit).toHaveBeenCalledWith(5);
  });
});
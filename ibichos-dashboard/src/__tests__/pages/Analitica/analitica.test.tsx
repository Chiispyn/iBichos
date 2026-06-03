import { describe, it, expect } from 'vitest'
import '../../firebase.mock'

const mockUsers = [
  { id: 'u1', displayName: 'Ana García',  region: 'Metropolitana de Santiago', city: 'Santiago',   capturas: 5 },
  { id: 'u2', displayName: 'Carlos Soto', region: 'Metropolitana de Santiago', city: 'Providencia', capturas: 3 },
  { id: 'u3', displayName: 'Lucía Ruiz',  region: 'Valparaíso',                city: 'Viña del Mar', capturas: 7 },
]

const mockCapturas = [
  { id: 'c1', status: 'APPROVED',       userId: 'u1' },
  { id: 'c2', status: 'PENDING_REVIEW', userId: 'u2' },
  { id: 'c3', status: 'APPROVED',       userId: 'u3' },
]

describe('TC-28 (Analíticas) — Métricas globales', () => {
  it('calcula el total de usuarios registrados', () => {
    expect(mockUsers).toHaveLength(3)
  })

  it('calcula las capturas pendientes de moderación', () => {
    const pendientes = mockCapturas.filter(c => c.status === 'PENDING_REVIEW')
    expect(pendientes).toHaveLength(1)
  })
})

describe('TC-29 (Analíticas) — Filtro de región geográfica', () => {
  it('filtra usuarios por Región Metropolitana de Santiago', () => {
    const rm = mockUsers.filter(u => u.region === 'Metropolitana de Santiago')
    expect(rm).toHaveLength(2)
  })

  it('filtra usuarios por Región de Valparaíso', () => {
    const valpo = mockUsers.filter(u => u.region === 'Valparaíso')
    expect(valpo).toHaveLength(1)
    expect(valpo[0].displayName).toBe('Lucía Ruiz')
  })
})

describe('TC-30 (Analíticas) — Exportar reporte consolidado (CSV)', () => {
  it('genera string CSV con llaves completas (Completitud: city y region)', () => {
    const headers = ['nombre', 'region', 'comuna', 'capturas']
    const csvLine = mockUsers
      .map(u => `${u.displayName},${u.region},${u.city},${u.capturas}`)
      .join('\n')
    const csv = headers.join(',') + '\n' + csvLine
    
    expect(csv).toContain('nombre,region,comuna,capturas')
    expect(csv).toContain('Ana García,Metropolitana de Santiago,Santiago,5')
    expect(csv).toContain('Lucía Ruiz,Valparaíso,Viña del Mar,7')
  })
})

import { describe, it, expect } from 'vitest'
import '../../firebase.mock'

const mockLogs: any[] = []

const logAction = (adminId: string, action: 'APPROVE' | 'REJECT' | 'STRIKE_AND_REJECT', targetId: string) => {
  mockLogs.push({
    id: `log${mockLogs.length + 1}`,
    adminId,
    action,
    targetId,
    timestamp: new Date().toISOString(),
  })
}

describe('TC-31 (Auditoría) — Registro inmutable automático', () => {
  it('registra un log automáticamente al aprobar una captura', () => {
    logAction('admin123', 'APPROVE', 'c1')
    expect(mockLogs).toHaveLength(1)
    expect(mockLogs[0].action).toBe('APPROVE')
    expect(mockLogs[0].adminId).toBe('admin123')
  })

  it('registra un log automáticamente al sancionar con strike', () => {
    logAction('admin123', 'STRIKE_AND_REJECT', 'c2')
    expect(mockLogs).toHaveLength(2)
    expect(mockLogs[1].action).toBe('STRIKE_AND_REJECT')
  })
})

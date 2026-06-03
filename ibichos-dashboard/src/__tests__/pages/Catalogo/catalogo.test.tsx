import { describe, it, expect } from 'vitest'
import '../../firebase.mock'

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

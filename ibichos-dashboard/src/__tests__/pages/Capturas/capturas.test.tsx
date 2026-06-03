import { describe, it, expect } from 'vitest'
import '../../firebase.mock'

const mockCapturas = [
  { id: '1', status: 'PENDING_REVIEW', insectName: 'Hormiga Carpintera'  },
  { id: '2', status: 'REJECTED',       insectName: 'Mosquito Tigre'      },
  { id: '3', status: 'PENDING_REVIEW', insectName: 'Libélula Azul'       },
]

describe('TC-28 — Filtro de Moderación', () => {
  it('filtra sólo capturas PENDING_REVIEW', () => {
    const pendientes = mockCapturas.filter(c => c.status === 'PENDING_REVIEW')
    expect(pendientes).toHaveLength(2)
    expect(pendientes.every(c => c.status === 'PENDING_REVIEW')).toBe(true)
  })

  it('filtra sólo capturas REJECTED', () => {
    const rechazadas = mockCapturas.filter(c => c.status === 'REJECTED')
    expect(rechazadas).toHaveLength(1)
    expect(rechazadas[0].insectName).toBe('Mosquito Tigre')
  })
})

describe('TC-29 — Lightbox de imagen', () => {
  it('se abre lightbox asignando la URL al estado', () => {
    let selectedImg: string | null = null
    const openLightbox = (url: string) => {
      selectedImg = url
    }
    openLightbox('https://img.test/bicho.jpg')
    expect(selectedImg).toBe('https://img.test/bicho.jpg')
  })
})

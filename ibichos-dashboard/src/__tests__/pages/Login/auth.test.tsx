import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useLogin } from '../../../pages/Login/useLogin'
import '../../firebase.mock'

// Mock react-router-dom navigate hook
const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

// Mock our custom auth context hook
const mockUseAuth = vi.fn()
vi.mock('../../context/authcontext', () => ({
  useAuth: () => mockUseAuth(),
}))

import * as firebaseAuth from 'firebase/auth'
import * as firebaseFirestore from 'firebase/firestore'

describe('Dashboard Auth Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseAuth.mockReturnValue({
      user: null,
      isAdminActive: false,
      username: '',
      loading: false,
    })
  })

  it('TC-01 — Login exitoso con admin activo', async () => {
    mockUseAuth.mockReturnValue({
      user: { uid: 'admin123' },
      isAdminActive: true,
      username: 'Felipe',
      loading: false,
    })

    const userCredential = { user: { uid: 'admin123' } }
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined)
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce(userCredential as any)

    const adminDocSnap = {
      exists: () => true,
      data: () => ({ status: 'active' }),
    }
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any)

    const { result } = renderHook(() => useLogin())

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any)
    })

    expect(firebaseAuth.signInWithEmailAndPassword).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/analitica')
  })

  it('TC-02 — Login bloqueado — usuario normal no admin', async () => {
    const userCredential = { user: { uid: 'normalUser123' } }
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined)
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce(userCredential as any)
    vi.spyOn(firebaseAuth, 'signOut').mockResolvedValueOnce(undefined)

    // Simula que el documento en la colección 'admins' no existe
    const adminDocSnap = {
      exists: () => false,
      data: () => null,
    }
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any)

    const { result } = renderHook(() => useLogin())

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any)
    })

    expect(firebaseAuth.signInWithEmailAndPassword).toHaveBeenCalled()
    expect(firebaseAuth.signOut).toHaveBeenCalled()
    expect(result.current.error).toBe('No tienes permisos de administrador para acceder aquí.')
  })

  it('TC-03 — Login bloqueado — admin pendiente (Ciclo 1 Fallido -> Ciclo 2 FIXED)', async () => {
    const userCredential = { user: { uid: 'pendingAdmin123' } }
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined)
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce(userCredential as any)
    vi.spyOn(firebaseAuth, 'signOut').mockResolvedValueOnce(undefined)

    // Simula que el admin existe pero su estatus es 'pending'
    const adminDocSnap = {
      exists: () => true,
      data: () => ({ status: 'pending' }),
    }
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any)

    const { result } = renderHook(() => useLogin())

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any)
    })

    expect(firebaseAuth.signInWithEmailAndPassword).toHaveBeenCalled()
    expect(firebaseAuth.signOut).toHaveBeenCalled()
    expect(result.current.error).toBe('Tu cuenta aún no ha sido aprobada por un administrador.')
  })

  it('TC-04 — Credenciales incorrectas', async () => {
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined)
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockRejectedValueOnce(new Error('auth/invalid-credential'))

    const { result } = renderHook(() => useLogin())

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any)
    })

    expect(firebaseAuth.signInWithEmailAndPassword).toHaveBeenCalled()
    expect(result.current.error).toBe('Correo o contraseña incorrectos.')
  })

  it('TC-05 — Recuperación de contraseña con email válido', async () => {
    vi.spyOn(firebaseAuth, 'sendPasswordResetEmail').mockResolvedValueOnce(undefined)

    const { result } = renderHook(() => useLogin())

    act(() => {
      result.current.setEmail('test@ibichos.cl')
    })

    await act(async () => {
      await result.current.handleResetPassword()
    })

    expect(firebaseAuth.sendPasswordResetEmail).toHaveBeenCalledWith(expect.anything(), 'test@ibichos.cl')
    expect(result.current.successMsg).toBe('Se ha enviado un enlace a tu correo para restablecer la contraseña.')
    expect(result.current.error).toBe('')
  })

  it('TC-06 — Recuperación sin email ingresado', async () => {
    const { result } = renderHook(() => useLogin())

    await act(async () => {
      await result.current.handleResetPassword()
    })

    expect(firebaseAuth.sendPasswordResetEmail).not.toHaveBeenCalled()
    expect(result.current.error).toBe('Por favor, ingresa tu correo electrónico en el campo superior para restablecer tu contraseña.')
  })
})

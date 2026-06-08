// src/__tests__/pages/Login/useLogin.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useLogin } from '../../../pages/Login/useLogin';

// Mocks de navegación y autenticación
const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

const mockUseAuth = vi.fn();
vi.mock('../../../context/authcontext', () => ({
  useAuth: () => mockUseAuth(),
}));

// Mocks de Firebase
import * as firebaseAuth from 'firebase/auth';
import * as firebaseFirestore from 'firebase/firestore';

vi.mock('firebase/auth', () => ({
  signInWithEmailAndPassword: vi.fn(),
  signOut: vi.fn(),
  sendPasswordResetEmail: vi.fn(),
  setPersistence: vi.fn(),
  browserLocalPersistence: 'LOCAL',
  browserSessionPersistence: 'SESSION',
  getAuth: vi.fn()
}));

vi.mock('firebase/firestore', () => ({
  doc: vi.fn(),
  getDoc: vi.fn(),
  getFirestore: vi.fn()
}));

vi.mock('../../../config/firebaseConfig', () => ({
  auth: {},
  db: {}
}));

describe('TC-61 — Cerebro Lógico del Login (useLogin)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
      isAdminActive: false,
      username: '',
      loading: false,
    });
  });

  it('TC-01 — Login exitoso con admin activo', async () => {
    mockUseAuth.mockReturnValue({ user: { uid: 'admin123' }, isAdminActive: true });

    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined);
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce({ user: { uid: 'admin123' } } as any);

    const adminDocSnap = { exists: () => true, data: () => ({ status: 'active' }) };
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any);

    const { result } = renderHook(() => useLogin());

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any);
    });

    expect(firebaseAuth.signInWithEmailAndPassword).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/analitica');
  });

  it('TC-02 — Login bloqueado — usuario normal no admin', async () => {
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined);
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce({ user: { uid: 'normal' } } as any);
    vi.spyOn(firebaseAuth, 'signOut').mockResolvedValueOnce(undefined);

    const adminDocSnap = { exists: () => false, data: () => null };
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any);

    const { result } = renderHook(() => useLogin());

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any);
    });

    expect(firebaseAuth.signOut).toHaveBeenCalled();
    expect(result.current.error).toBe('No tienes permisos de administrador para acceder aquí.');
  });

  it('TC-03 — Login bloqueado — admin pendiente', async () => {
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined);
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockResolvedValueOnce({ user: { uid: 'pending' } } as any);
    vi.spyOn(firebaseAuth, 'signOut').mockResolvedValueOnce(undefined);

    const adminDocSnap = { exists: () => true, data: () => ({ status: 'pending' }) };
    vi.spyOn(firebaseFirestore, 'getDoc').mockResolvedValueOnce(adminDocSnap as any);

    const { result } = renderHook(() => useLogin());

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any);
    });

    expect(firebaseAuth.signOut).toHaveBeenCalled();
    expect(result.current.error).toBe('Tu cuenta aún no ha sido aprobada por un administrador.');
  });

  it('TC-04 — Credenciales incorrectas', async () => {
    vi.spyOn(firebaseAuth, 'setPersistence').mockResolvedValueOnce(undefined);
    vi.spyOn(firebaseAuth, 'signInWithEmailAndPassword').mockRejectedValueOnce(new Error('auth/invalid-credential'));

    const { result } = renderHook(() => useLogin());

    await act(async () => {
      await result.current.handleLogin({ preventDefault: () => {} } as any);
    });

    expect(result.current.error).toBe('Correo o contraseña incorrectos.');
  });

  it('TC-05 — Recuperación de contraseña con email válido', async () => {
    vi.spyOn(firebaseAuth, 'sendPasswordResetEmail').mockResolvedValueOnce(undefined);
    const { result } = renderHook(() => useLogin());

    act(() => { result.current.setEmail('test@ibichos.cl'); });

    await act(async () => {
      await result.current.handleResetPassword();
    });

    expect(firebaseAuth.sendPasswordResetEmail).toHaveBeenCalledWith(expect.anything(), 'test@ibichos.cl');
    expect(result.current.successMsg).toBe('Se ha enviado un enlace a tu correo para restablecer la contraseña.');
  });

  it('TC-06 — Recuperación sin email ingresado', async () => {
    const { result } = renderHook(() => useLogin());

    await act(async () => {
      await result.current.handleResetPassword();
    });

    expect(firebaseAuth.sendPasswordResetEmail).not.toHaveBeenCalled();
    expect(result.current.error).toBe('Por favor, ingresa tu correo electrónico en el campo superior para restablecer tu contraseña.');
  });
});
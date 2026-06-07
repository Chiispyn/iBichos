// src/__tests__/pages/Login/login.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { Login } from '../../../pages/Login/login';
import { useLogin } from '../../../pages/Login/useLogin';

// 1. MOCK DEL HOOK
vi.mock('../../../pages/Login/useLogin');

describe('TC-60 — Módulo de Login (Interfaz y Eventos UI)', () => {
  const mockSetEmail = vi.fn();
  const mockSetPassword = vi.fn();
  const mockSetRememberMe = vi.fn();
  const mockHandleLogin = vi.fn((e) => e.preventDefault());
  const mockHandleResetPassword = vi.fn();

  const baseMock = {
    email: '',
    setEmail: mockSetEmail,
    password: '',
    setPassword: mockSetPassword,
    error: '',
    successMsg: '',
    rememberMe: false,
    setRememberMe: mockSetRememberMe,
    handleLogin: mockHandleLogin,
    handleResetPassword: mockHandleResetPassword
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Renderiza el formulario de inicio de sesión correctamente', () => {
    (useLogin as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Login /></BrowserRouter>);

    expect(screen.getByText('Administración iBichos')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('admin@ibichos.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
  });

  test('Los inputs actualizan el estado del hook al escribir', async () => {
    const user = userEvent.setup();
    (useLogin as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Login /></BrowserRouter>);

    const inputEmail = screen.getByPlaceholderText('admin@ibichos.com');
    const inputPassword = screen.getByPlaceholderText('••••••••');
    const checkboxRemember = screen.getByLabelText('Mantener sesión iniciada');

    await user.type(inputEmail, 'felipe@admin.cl');
    await user.type(inputPassword, 'secreta123');
    await user.click(checkboxRemember);

    expect(mockSetEmail).toHaveBeenCalled();
    expect(mockSetPassword).toHaveBeenCalled();
    expect(mockSetRememberMe).toHaveBeenCalledWith(true);
  });

  test('Envía el formulario y ejecuta handleLogin', async () => {
    const user = userEvent.setup();
    (useLogin as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Login /></BrowserRouter>);

    // 1. Buscamos los campos obligatorios
    const inputEmail = screen.getByPlaceholderText('admin@ibichos.com');
    const inputPassword = screen.getByPlaceholderText('••••••••');
    
    // 2. Los llenamos para pasar la validación "required" del HTML
    await user.type(inputEmail, 'admin@bichos.cl');
    await user.type(inputPassword, '123456');

    // 3. Ahora sí hacemos clic en enviar
    const btnSubmit = screen.getByRole('button', { name: 'Entrar al Panel' });
    await user.click(btnSubmit);

    // 4. El formulario se envía con éxito
    expect(mockHandleLogin).toHaveBeenCalledTimes(1);
  });

  test('Hace clic en el botón de recuperar contraseña', async () => {
    const user = userEvent.setup();
    (useLogin as any).mockReturnValue(baseMock);
    render(<BrowserRouter><Login /></BrowserRouter>);

    const btnReset = screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' });
    await user.click(btnReset);

    expect(mockHandleResetPassword).toHaveBeenCalledTimes(1);
  });

  test('Muestra el banner de error si el hook devuelve un mensaje de error', () => {
    (useLogin as any).mockReturnValue({ ...baseMock, error: 'Credenciales inválidas' });
    render(<BrowserRouter><Login /></BrowserRouter>);

    expect(screen.getByText('Credenciales inválidas')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('alert-danger');
  });

  test('Muestra el banner de éxito si el hook devuelve un mensaje de éxito', () => {
    (useLogin as any).mockReturnValue({ ...baseMock, successMsg: 'Correo enviado con éxito' });
    render(<BrowserRouter><Login /></BrowserRouter>);

    expect(screen.getByText('Correo enviado con éxito')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('alert-success');
  });
});
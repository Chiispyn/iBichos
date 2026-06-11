import { renderHook, act } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../../context/authcontext';
import { getDoc } from 'firebase/firestore';

// -------------------------------------------------------------------------
// 1. SECUESTRO DE FIREBASE (MOCKS AVANZADOS)
// Extraemos los callbacks de Firebase para ejecutarlos "a control remoto"
// -------------------------------------------------------------------------
let mockAuthCallback: (user: any) => void;
let mockSnapshotCallback: (doc: any) => void;
let mockSnapshotErrorCallback: (error: any) => void;

const mockUnsubscribeAuth = vi.fn();
const mockUnsubscribeSnapshot = vi.fn();

vi.mock('firebase/auth', () => ({
  getAuth: vi.fn(() => ({})), // <--- ¡AQUÍ ESTÁ LA CORRECCIÓN CLAVE!
  onAuthStateChanged: vi.fn((_auth, callback) => {
    mockAuthCallback = callback; // Guardamos la función para dispararla después
    return mockUnsubscribeAuth;
  }),
}));

vi.mock('firebase/firestore', () => ({
  getFirestore: vi.fn(() => ({})), 
  doc: vi.fn((_db, collection, id) => `${collection}/${id}`),
  getDoc: vi.fn(),
  onSnapshot: vi.fn((_ref, onNext, onError) => {
    mockSnapshotCallback = onNext;     
    mockSnapshotErrorCallback = onError; 
    return mockUnsubscribeSnapshot;
  }),
}));

vi.mock('../../../config/firebaseConfig', () => ({
  auth: {},
  db: {},
}));

describe('Lógica de Negocio y Seguridad: AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(console, 'error').mockImplementation(() => {}); // Silenciar errores provocados
  });

  // Wrapper necesario para testear el custom hook dentro de su Provider
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  );

  // -------------------------------------------------------------------------
  // 2. BATERÍA DE PRUEBAS FUNCIONALES
  // -------------------------------------------------------------------------

  test('Funcionalidad: Estado de arranque seguro', () => {
    // Al iniciar, la app DEBE estar en modo "cargando" y sin permisos concedidos
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    expect(result.current.loading).toBe(true);
    expect(result.current.user).toBeNull();
    expect(result.current.isAdminActive).toBe(false);
  });

  test('Funcionalidad: Manejo de usuario invitado (No Autenticado)', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    // Simulamos que Firebase detecta que NO hay sesión activa
    act(() => {
      mockAuthCallback(null);
    });

    // El sistema debe detener la carga, mantener el usuario nulo y negar permisos
    expect(result.current.loading).toBe(false);
    expect(result.current.user).toBeNull();
    expect(result.current.isAdminActive).toBe(false);
  });

  test('Funcionalidad: Inicio de sesión exitoso como Administrador Activo', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    // 1. Firebase detecta una sesión válida
    const mockUser = { uid: 'admin_123', email: 'felipe@ibichos.cl' };
    act(() => { mockAuthCallback(mockUser); });

    // 2. Simulamos que el getDoc devolverá el nombre completo del usuario
    vi.mocked(getDoc).mockResolvedValueOnce({
      exists: () => true,
      data: () => ({ displayName: 'Felipe Admin' })
    } as any);

    // 3. Firestore confirma en tiempo real que el usuario tiene estado "active"
    await act(async () => {
      mockSnapshotCallback({
        exists: () => true,
        data: () => ({ status: 'active' })
      });
    });

    // Validamos que se hayan concedido las credenciales completas
    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAdminActive).toBe(true);
    expect(result.current.username).toBe('Felipe Admin');
    expect(result.current.loading).toBe(false);
  });

  test('Funcionalidad: Fallback de nombre si falta en Firestore', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    act(() => { mockAuthCallback({ uid: 'admin_123', email: 'felipe@ibichos.cl' }); });

    // Simulamos que el usuario EXISTE pero NO tiene "displayName" configurado
    vi.mocked(getDoc).mockResolvedValueOnce({
      exists: () => true,
      data: () => ({ /* sin displayName */ })
    } as any);

    await act(async () => {
      mockSnapshotCallback({ exists: () => true, data: () => ({ status: 'active' }) });
    });

    // Debe extraer el nombre desde el correo ("felipe")
    expect(result.current.username).toBe('felipe');
  });

  test('Seguridad: Denegación de acceso a usuarios registrados pero sin rol de Admin', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    act(() => { mockAuthCallback({ uid: 'user_normi' }); });

    // Simulamos que la colección "admins" dice que el usuario está "inactive" (o suspendido)
    await act(async () => {
      mockSnapshotCallback({
        exists: () => true,
        data: () => ({ status: 'inactive' })
      });
    });

    // Validamos la regla crítica de seguridad: Permiso denegado
    expect(result.current.isAdminActive).toBe(false);
    expect(result.current.username).toBe('');
  });

  test('Seguridad: Cierre hermético ante errores de base de datos (Red caída)', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    act(() => { mockAuthCallback({ uid: 'admin_123' }); });

    // Simulamos que Firestore tira un error (ej. se cayó el internet)
    await act(async () => {
      mockSnapshotErrorCallback(new Error('Firestore desconectado'));
    });

    // Ante la duda, la seguridad DEBE cerrarse. Falla seguro (Fail-Safe)
    expect(result.current.isAdminActive).toBe(false);
    expect(result.current.loading).toBe(false);
  });

  test('Rendimiento: Limpieza de listeners para evitar Memory Leaks al desmontar', () => {
    const { unmount } = renderHook(() => useAuth(), { wrapper });
    
    // Inicia sesión para activar el onSnapshot
    act(() => { mockAuthCallback({ uid: 'admin_123' }); });

    // Desmontamos el contexto (ej: el usuario cierra la app)
    unmount();

    // Verificamos que se ejecutaron las funciones de limpieza de Firebase
    expect(mockUnsubscribeAuth).toHaveBeenCalled();
    expect(mockUnsubscribeSnapshot).toHaveBeenCalled();
  });
});
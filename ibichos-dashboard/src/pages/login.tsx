import { useState, useEffect } from 'react'; // 🟢 Agrega useEffect
import { signInWithEmailAndPassword, signOut, sendPasswordResetEmail, setPersistence, browserLocalPersistence, browserSessionPersistence } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { auth, db } from '../config/firebaseConfig';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authcontext'; // 🟢 Importa el contexto

export function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  
  const navigate = useNavigate();
  // 🟢 Traemos el usuario y su estado de admin
  const { user, isAdminActive } = useAuth(); 

  // 🟢 EFECTO: Si ya hay un admin activo logueado, sácalo de la vista de login y mándalo al panel
  useEffect(() => {
    if (user && isAdminActive) {
      navigate('/principal');
    }
  }, [user, isAdminActive, navigate]);

  const handleResetPassword = async () => {
    if (!email) {
      setError('Por favor, ingresa tu correo electrónico en el campo superior para restablecer tu contraseña.');
      setSuccessMsg('');
      return;
    }
    try {
      await sendPasswordResetEmail(auth, email);
      setSuccessMsg('Se ha enviado un enlace a tu correo para restablecer la contraseña.');
      setError('');
    } catch (err: any) {
      setError('Error al intentar enviar el correo. Verifica que esté bien escrito.');
      setSuccessMsg('');
      console.error(err);
    }
  };

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
  e.preventDefault();

    try {
      // Configurar la persistencia de la sesión según el checkbox
      await setPersistence(auth, rememberMe ? browserLocalPersistence : browserSessionPersistence);

      // 1. Intento de inicio de sesión en Auth
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;

      // 2. Buscar al usuario en la colección 'admins' de Firestore
      const adminDocRef = doc(db, "admins", user.uid);
      const adminDoc = await getDoc(adminDocRef);

      if (adminDoc.exists()) {
        const data = adminDoc.data();

        // 3. Verificar si está activo
        if (data.status === 'active') {
          console.log("¡Bienvenido, Admin!");
          navigate('/analitica'); // Redirigir al panel de analítica
        } else {
          // Si existe pero está pendiente
          setError("Tu cuenta aún no ha sido aprobada por un administrador.");
          await signOut(auth); // Cerramos sesión por seguridad
        }
      } else {
        // Si no existe en la colección 'admins' (es un usuario de la app móvil)
        setError("No tienes permisos de administrador para acceder aquí.");
        await signOut(auth);
      }
    } catch (err) {
      setError("Correo o contraseña incorrectos.");
      console.error(err);
    }
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100" style={{ backgroundColor: '#E8F5E9' }}>
      <div className="card border-0 shadow-lg" style={{ width: '100%', maxWidth: '420px', borderRadius: '1.5rem', overflow: 'hidden' }}>
        <div className="card-header border-0 text-center pt-5 pb-3 bg-white">
          <img src="/logo_oficial.png" alt="iBichos Logo" className="img-fluid mb-3 drop-shadow" style={{ maxHeight: '110px' }} />
          <h4 className="fw-bolder text-dark mb-0" style={{ letterSpacing: '-0.5px' }}>Administración iBichos</h4>
          <p className="text-secondary small mb-0 mt-1">Acceso seguro para moderadores</p>
        </div>
        <div className="card-body p-4 p-md-5 bg-white pt-2">
          <form onSubmit={handleLogin}>
            {error && (
              <div className="alert alert-danger py-2 border-0 rounded-3 text-center small fw-bold mb-4" role="alert" style={{ backgroundColor: '#ffebee', color: '#c62828' }}>
                {error}
              </div>
            )}
            {successMsg && (
              <div className="alert alert-success py-2 border-0 rounded-3 text-center small fw-bold mb-4" role="alert" style={{ backgroundColor: '#e8f5e9', color: '#2e7d32' }}>
                {successMsg}
              </div>
            )}
            <div className="mb-3">
              <label className="form-label text-muted small fw-bold mb-1">Correo Electrónico</label>
              <input 
                type="email" 
                className="form-control form-control-lg bg-light border-0 shadow-none"
                placeholder="admin@ibichos.com" 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                style={{ fontSize: '15px', borderRadius: '0.8rem' }}
              />
            </div>
            <div className="mb-4">
              <label className="form-label text-muted small fw-bold mb-1">Contraseña</label>
              <input 
                type="password" 
                className="form-control form-control-lg bg-light border-0 shadow-none"
                placeholder="••••••••" 
                onChange={(e) => setPassword(e.target.value)} 
                required 
                style={{ fontSize: '15px', borderRadius: '0.8rem' }}
              />
            </div>
            
            <div className="form-check mb-4 d-flex align-items-center">
              <input 
                className="form-check-input me-2 shadow-none" 
                type="checkbox" 
                id="rememberMe" 
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                style={{ cursor: 'pointer', accentColor: '#3DDC84' }}
              />
              <label className="form-check-label text-muted small fw-medium mt-1" htmlFor="rememberMe" style={{ cursor: 'pointer' }}>
                Mantener sesión iniciada
              </label>
            </div>

            <button 
              type="submit" 
              className="btn w-100 py-3 rounded-3 fw-bold text-white shadow"
              style={{ backgroundColor: '#3DDC84', border: 'none', fontSize: '16px', borderRadius: '0.8rem', transition: 'all 0.3s' }}
              onMouseOver={(e) => {
                e.currentTarget.style.backgroundColor = '#2ebc6e';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.backgroundColor = '#3DDC84';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              Entrar al Panel
            </button>
            <div className="text-center mt-4">
              <button 
                type="button" 
                className="btn btn-link text-success text-decoration-none small fw-bold shadow-none p-0"
                onClick={handleResetPassword}
              >
                ¿Olvidaste tu contraseña?
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
import { useState, useEffect } from 'react'; // 🟢 Agrega useEffect
import { signInWithEmailAndPassword, signOut } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { auth, db } from '../config/firebaseConfig';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authcontext'; // 🟢 Importa el contexto

export function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const navigate = useNavigate();
  // 🟢 Traemos el usuario y su estado de admin
  const { user, isAdminActive } = useAuth(); 

  // 🟢 EFECTO: Si ya hay un admin activo logueado, sácalo de la vista de login y mándalo al panel
  useEffect(() => {
    if (user && isAdminActive) {
      navigate('/principal');
    }
  }, [user, isAdminActive, navigate]);

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
  e.preventDefault();

    try {
      // 1. Intento de inicio de sesión en Auth
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;

      // 2. Buscar al usuario en la colección 'admins' de Firestore
      const adminDocRef = doc(db, "admins", user.uid);
      const adminDoc = await getDoc(adminDocRef);

      if (adminDoc.exists()) {
        const data = adminDoc.data();

        // 3. Verificar si está activo
        if (data.estado === 'activo') {
          console.log("¡Bienvenido, Admin!");
          navigate('/dashboard'); // Redirigir al panel
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
    <div className="login-container">
      <form onSubmit={handleLogin}>
        <h2>Panel de Control Web</h2>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <input 
          type="email" 
          placeholder="Correo electrónico" 
          onChange={(e) => setEmail(e.target.value)} 
          required 
        />
        <input 
          type="password" 
          placeholder="Contraseña" 
          onChange={(e) => setPassword(e.target.value)} 
          required 
        />
        <button type="submit">Entrar al Panel</button>
      </form>
    </div>
  );
}
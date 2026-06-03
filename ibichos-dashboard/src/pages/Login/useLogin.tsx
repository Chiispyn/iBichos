import { useState, useEffect } from 'react';
import { signInWithEmailAndPassword, signOut, sendPasswordResetEmail, setPersistence, browserLocalPersistence, browserSessionPersistence } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { auth, db } from '../../config/firebaseConfig';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authcontext';

export function useLogin() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  
  const navigate = useNavigate();
  const { user, isAdminActive } = useAuth(); 

  // EFECTO: Redirección automática si ya está logueado
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
      await setPersistence(auth, rememberMe ? browserLocalPersistence : browserSessionPersistence);
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;

      const adminDocRef = doc(db, "admins", user.uid);
      const adminDoc = await getDoc(adminDocRef);

      if (adminDoc.exists()) {
        const data = adminDoc.data();
        if (data.status === 'active') {
          console.log("¡Bienvenido, Admin!");
          navigate('/analitica'); 
        } else {
          setError("Tu cuenta aún no ha sido aprobada por un administrador.");
          await signOut(auth);
        }
      } else {
        setError("No tienes permisos de administrador para acceder aquí.");
        await signOut(auth);
      }
    } catch (err) {
      setError("Correo o contraseña incorrectos.");
      console.error(err);
    }
  };

  return {
    email,
    setEmail,
    password,
    setPassword,
    error,
    successMsg,
    rememberMe,
    setRememberMe,
    handleLogin,
    handleResetPassword
  };
}
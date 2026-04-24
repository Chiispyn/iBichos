import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { onAuthStateChanged, type User } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { auth, db } from '../config/firebaseConfig';

interface AuthContextType {
  user: User | null;
  isAdminActive: boolean;
  username: string;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType>({ user: null, isAdminActive: false, username: '', loading: true });

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAdminActive, setIsAdminActive] = useState(false);
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Escucha cambios en la sesión (login/logout)
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      setUser(currentUser);
      
      if (currentUser) {
        // Verificamos en Firestore si es un admin activo
        const adminDoc = await getDoc(doc(db, "admins", currentUser.uid));
        if (adminDoc.exists() && adminDoc.data().estado === 'activo') {
          setIsAdminActive(true);
          // Intentar obtener el username desde la colección de usuarios
          const userDoc = await getDoc(doc(db, "users", currentUser.uid));
          if (userDoc.exists() && userDoc.data().displayName) {
            setUsername(userDoc.data().displayName);
          } else if (currentUser.displayName) {
            setUsername(currentUser.displayName);
          } else {
            setUsername(currentUser.email?.split('@')[0] || 'Admin');
          }
        } else {
          setIsAdminActive(false);
          setUsername('');
        }
      } else {
        setIsAdminActive(false);
        setUsername('');
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAdminActive, username, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
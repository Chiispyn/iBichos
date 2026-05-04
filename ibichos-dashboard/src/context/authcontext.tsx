import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { onAuthStateChanged, type User } from 'firebase/auth';
import { doc, getDoc, onSnapshot } from 'firebase/firestore';
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
    let unsubscribeSnapshot: (() => void) | null = null;

    // Escucha cambios en la sesión (login/logout)
    const unsubscribeAuth = onAuthStateChanged(auth, async (currentUser) => {
      // Limpiar snapshot anterior si existe
      if (unsubscribeSnapshot) {
        unsubscribeSnapshot();
        unsubscribeSnapshot = null;
      }

      setUser(currentUser);
      
      if (currentUser) {
        // Escuchamos en TIEMPO REAL el documento del admin
        unsubscribeSnapshot = onSnapshot(doc(db, "admins", currentUser.uid), async (adminDoc) => {
          if (adminDoc.exists() && adminDoc.data().estado === 'activo') {
            setIsAdminActive(true);
            
            // Obtenemos el username (esto puede ser una vez por sesión)
            const userDoc = await getDoc(doc(db, "users", currentUser.uid));
            if (userDoc.exists() && userDoc.data().displayName) {
              setUsername(userDoc.data().displayName);
            } else {
              setUsername(currentUser.displayName || currentUser.email?.split('@')[0] || 'Admin');
            }
          } else {
            // Si el doc no existe o el estado no es activo, quitamos permisos inmediatamente
            setIsAdminActive(false);
            setUsername('');
          }
          setLoading(false);
        }, (error) => {
          console.error("Error en snapshot de permisos:", error);
          setIsAdminActive(false);
          setLoading(false);
        });
      } else {
        setIsAdminActive(false);
        setUsername('');
        setLoading(false);
      }
    });

    return () => {
      unsubscribeAuth();
      if (unsubscribeSnapshot) unsubscribeSnapshot();
    };
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAdminActive, username, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
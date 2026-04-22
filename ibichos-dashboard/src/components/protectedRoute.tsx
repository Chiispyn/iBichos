import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/authcontext';
import type { JSX } from 'react';

export const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { user, isAdminActive, loading } = useAuth();

  if (loading) return <p>Cargando sesión...</p>;

  // Si no hay usuario o no es un admin activo, lo mandamos al login (/)
  if (!user || !isAdminActive) {
    return <Navigate to="/" replace />;
  }

  return children;
};
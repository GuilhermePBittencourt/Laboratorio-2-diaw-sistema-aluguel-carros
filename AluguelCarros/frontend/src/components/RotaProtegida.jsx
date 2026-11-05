import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Exige login. Se "perfis" for informado, só deixa passar quem tem um desses perfis.
function RotaProtegida({ perfis }) {
  const { autenticado, perfil } = useAuth();
  const local = useLocation();

  if (!autenticado) {
    return <Navigate to="/login" replace state={{ de: local.pathname }} />;
  }
  if (perfis && !perfis.includes(perfil)) {
    return <Navigate to="/pedidos" replace />;
  }
  return <Outlet />;
}

export default RotaProtegida;

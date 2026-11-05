import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import Alerta from '../components/Alerta';
import { useAuth } from '../context/AuthContext';

function Login() {
  const { autenticado, entrar } = useAuth();
  const navigate = useNavigate();
  const local = useLocation();

  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (autenticado) {
    return <Navigate to="/pedidos" replace />;
  }

  async function handleSubmit(evento) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(login.trim(), senha);
      navigate(local.state?.de ?? '/pedidos', { replace: true });
    } catch (e) {
      setErro(e);
      setEnviando(false);
    }
  }

  return (
    <main className="pagina pagina-estreita">
      <div className="cartao">
        <h1>Entrar</h1>
        <p className="texto-suave">Use seu usuário e senha para acessar o sistema.</p>

        <form className="formulario" onSubmit={handleSubmit}>
          <label>
            Usuário
            <input value={login} onChange={(e) => setLogin(e.target.value)} required autoComplete="username"
              autoFocus />
          </label>
          <label>
            Senha
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required
              autoComplete="current-password" />
          </label>

          <Alerta erro={erro} />

          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <p className="texto-suave centralizado">
          Ainda não tem cadastro? <Link to="/registro">Cadastre-se</Link>
        </p>
      </div>
    </main>
  );
}

export default Login;

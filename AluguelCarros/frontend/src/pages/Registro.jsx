import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import ClienteForm from '../components/ClienteForm';
import { useAuth } from '../context/AuthContext';

function Registro() {
  const { autenticado, registrar } = useAuth();
  const navigate = useNavigate();
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (autenticado) {
    return <Navigate to="/pedidos" replace />;
  }

  async function handleSubmit(dados) {
    setErro(null);
    setEnviando(true);
    try {
      await registrar(dados);
      navigate('/pedidos', { replace: true });
    } catch (e) {
      setErro(e);
      setEnviando(false);
    }
  }

  return (
    <main className="pagina pagina-media">
      <div className="cartao">
        <h1>Cadastre-se</h1>
        <p className="texto-suave">
          O cadastro é necessário para usar o sistema. Depois dele você já entra automaticamente.
        </p>
        <ClienteForm comCredenciais rotuloBotao="Criar cadastro" onSubmit={handleSubmit} enviando={enviando}
          erro={erro} />
        <p className="texto-suave centralizado">
          Já tem cadastro? <Link to="/login">Entrar</Link>
        </p>
      </div>
    </main>
  );
}

export default Registro;

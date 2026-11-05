import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Header() {
  const { autenticado, ehCliente, ehAgente, nome, sair } = useAuth();
  const navigate = useNavigate();

  function handleSair() {
    sair();
    navigate('/login');
  }

  return (
    <header className="topo">
      <div className="topo-conteudo">
        <NavLink to="/" className="marca">
          <span className="marca-icone" aria-hidden="true" />
          Aluguel de Carros
        </NavLink>

        <nav className="menu" aria-label="Navegação principal">
          {ehCliente && (
            <>
              <NavLink to="/pedidos" end>Meus pedidos</NavLink>
              <NavLink to="/pedidos/novo">Novo pedido</NavLink>
              <NavLink to="/automoveis">Automóveis</NavLink>
              <NavLink to="/perfil">Meu cadastro</NavLink>
            </>
          )}
          {ehAgente && (
            <>
              <NavLink to="/pedidos" end>Pedidos</NavLink>
              <NavLink to="/clientes">Clientes</NavLink>
              <NavLink to="/automoveis">Automóveis</NavLink>
            </>
          )}
        </nav>

        <div className="topo-usuario">
          {autenticado ? (
            <>
              <span className="topo-nome">{nome}</span>
              <button type="button" className="botao botao-contorno botao-pequeno" onClick={handleSair}>
                Sair
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="botao botao-contorno botao-pequeno">Entrar</NavLink>
              <NavLink to="/registro" className="botao botao-pequeno">Cadastrar-se</NavLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;

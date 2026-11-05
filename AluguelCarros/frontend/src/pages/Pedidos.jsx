import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../components/Alerta';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { ROTULO_STATUS, descreverAutomovel, formatarData } from '../utils/formatadores';

function Pedidos() {
  const { ehCliente, ehAgente } = useAuth();
  const [pedidos, setPedidos] = useState(null);
  const [filtro, setFiltro] = useState('');
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let ativo = true;
    api
      .listarPedidos(filtro)
      .then((dados) => {
        if (ativo) {
          setPedidos(dados);
          setErro(null);
        }
      })
      .catch((e) => ativo && setErro(e));
    return () => {
      ativo = false;
    };
  }, [filtro]);

  return (
    <main className="pagina">
      <div className="cabecalho-pagina">
        <div>
          <h1>{ehCliente ? 'Meus pedidos' : 'Pedidos de aluguel'}</h1>
          <p className="texto-suave">
            {ehCliente
              ? 'Acompanhe aqui o status de cada pedido.'
              : 'Avalie os pedidos em análise e execute os contratos dos aprovados.'}
          </p>
        </div>
        {ehCliente && <Link to="/pedidos/novo" className="botao">Novo pedido</Link>}
      </div>

      <div className="filtros">
        <label>
          Status
          <select value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="">Todos</option>
            {Object.entries(ROTULO_STATUS).map(([valor, rotulo]) => (
              <option key={valor} value={valor}>{rotulo}</option>
            ))}
          </select>
        </label>
      </div>

      <Alerta erro={erro} />

      {pedidos === null && !erro && <p className="texto-suave">Carregando pedidos...</p>}

      {pedidos?.length === 0 && (
        <div className="vazio">
          <p>{filtro ? 'Nenhum pedido com este status.' : 'Nenhum pedido por aqui ainda.'}</p>
          {ehCliente && !filtro && <Link to="/pedidos/novo" className="botao">Fazer o primeiro pedido</Link>}
        </div>
      )}

      {pedidos?.length > 0 && (
        <div className="tabela-rolavel">
          <table className="tabela">
            <thead>
              <tr>
                <th>Pedido</th>
                {ehAgente && <th>Cliente</th>}
                <th>Automóvel</th>
                <th>Período</th>
                <th>Status</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {pedidos.map((pedido) => (
                <tr key={pedido.id}>
                  <td>#{pedido.id}</td>
                  {ehAgente && <td>{pedido.clienteNome}</td>}
                  <td>{descreverAutomovel(pedido.automovel)}</td>
                  <td>{formatarData(pedido.dataInicio)} a {formatarData(pedido.dataFim)}</td>
                  <td><StatusBadge status={pedido.status} /></td>
                  <td className="coluna-acoes">
                    <Link to={`/pedidos/${pedido.id}`} className="botao botao-contorno botao-pequeno">
                      Detalhes
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}

export default Pedidos;

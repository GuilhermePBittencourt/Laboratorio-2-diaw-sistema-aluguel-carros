import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Alerta from '../components/Alerta';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import {
  ROTULO_PROPRIETARIO,
  descreverAutomovel,
  formatarData,
  formatarDataHora,
  formatarMoeda,
} from '../utils/formatadores';

const ETAPAS = ['EM_ANALISE', 'APROVADO', 'CONTRATADO'];
const ROTULO_ETAPA = { EM_ANALISE: 'Em análise', APROVADO: 'Aprovado', CONTRATADO: 'Contratado' };

// Linha do tempo: mostra em que etapa o pedido está (reprovado e cancelado encerram o fluxo).
function Etapas({ status }) {
  if (status === 'REPROVADO' || status === 'CANCELADO') {
    return <p className="texto-suave">Este pedido foi encerrado e não segue para contrato.</p>;
  }
  const atual = ETAPAS.indexOf(status);
  return (
    <ol className="etapas">
      {ETAPAS.map((etapa, indice) => (
        <li key={etapa} className={indice <= atual ? 'etapa etapa-feita' : 'etapa'}>
          {ROTULO_ETAPA[etapa]}
        </li>
      ))}
    </ol>
  );
}

function PedidoDetalhe() {
  const { id } = useParams();
  const { ehCliente, ehAgente } = useAuth();

  const [pedido, setPedido] = useState(null);
  const [bancos, setBancos] = useState([]);
  const [erro, setErro] = useState(null);
  const [ocupado, setOcupado] = useState(false);

  // formulário de avaliação
  const [parecer, setParecer] = useState('');
  // formulário de contrato
  const [proprietario, setProprietario] = useState('EMPRESA');
  const [comCredito, setComCredito] = useState(false);
  const [bancoId, setBancoId] = useState('');
  const [valorCredito, setValorCredito] = useState('');

  useEffect(() => {
    let ativo = true;
    api
      .buscarPedido(id)
      .then((dados) => ativo && setPedido(dados))
      .catch((e) => ativo && setErro(e));
    return () => {
      ativo = false;
    };
  }, [id]);

  useEffect(() => {
    if (!ehAgente) return;
    let ativo = true;
    api
      .listarAgentes('BANCO')
      .then((lista) => ativo && setBancos(lista))
      .catch(() => ativo && setBancos([]));
    return () => {
      ativo = false;
    };
  }, [ehAgente]);

  // Executa uma ação sobre o pedido e atualiza a tela com a resposta.
  async function executar(acao) {
    setErro(null);
    setOcupado(true);
    try {
      setPedido(await acao());
    } catch (e) {
      setErro(e);
    } finally {
      setOcupado(false);
    }
  }

  function handleCancelar() {
    if (window.confirm('Cancelar este pedido?')) {
      executar(() => api.cancelarPedido(id));
    }
  }

  function handleAvaliar(decisao) {
    executar(() => api.avaliarPedido(id, { decisao, parecer }));
  }

  function handleContratar(evento) {
    evento.preventDefault();
    const dados = { proprietario };
    if (comCredito) {
      dados.bancoCreditoId = Number(bancoId);
      dados.valorCredito = Number(valorCredito);
    }
    executar(() => api.contratarPedido(id, dados));
  }

  if (!pedido) {
    return (
      <main className="pagina">
        <Alerta erro={erro} />
        {!erro && <p className="texto-suave">Carregando pedido...</p>}
        <Link to="/pedidos" className="botao botao-contorno">Voltar aos pedidos</Link>
      </main>
    );
  }

  const emAnalise = pedido.status === 'EM_ANALISE';
  const aprovado = pedido.status === 'APROVADO';
  const podeCancelar = ehCliente && (emAnalise || aprovado);

  return (
    <main className="pagina pagina-media">
      <Link to="/pedidos" className="voltar">&larr; Pedidos</Link>

      <div className="cabecalho-pagina">
        <h1>Pedido #{pedido.id}</h1>
        <StatusBadge status={pedido.status} />
      </div>

      <section className="cartao">
        <Etapas status={pedido.status} />

        <dl className="detalhes">
          <div>
            <dt>Automóvel</dt>
            <dd>{descreverAutomovel(pedido.automovel)}</dd>
          </div>
          <div>
            <dt>Período</dt>
            <dd>{formatarData(pedido.dataInicio)} a {formatarData(pedido.dataFim)}</dd>
          </div>
          {ehAgente && (
            <div>
              <dt>Cliente</dt>
              <dd>{pedido.clienteNome}</dd>
            </div>
          )}
          <div>
            <dt>Registrado em</dt>
            <dd>{formatarDataHora(pedido.criadoEm)}</dd>
          </div>
          {pedido.avaliadoPor && (
            <div>
              <dt>Avaliado por</dt>
              <dd>{pedido.avaliadoPor}</dd>
            </div>
          )}
          {pedido.parecer && (
            <div className="detalhe-largo">
              <dt>Parecer financeiro</dt>
              <dd>{pedido.parecer}</dd>
            </div>
          )}
        </dl>

        {pedido.contrato && (
          <div className="contrato">
            <h2>Contrato</h2>
            <dl className="detalhes">
              <div>
                <dt>Automóvel registrado em nome de</dt>
                <dd>{ROTULO_PROPRIETARIO[pedido.contrato.proprietario]}</dd>
              </div>
              <div>
                <dt>Executado em</dt>
                <dd>{formatarDataHora(pedido.contrato.dataContrato)}</dd>
              </div>
              {pedido.contrato.bancoCredito && (
                <>
                  <div>
                    <dt>Crédito concedido por</dt>
                    <dd>{pedido.contrato.bancoCredito}</dd>
                  </div>
                  <div>
                    <dt>Valor do crédito</dt>
                    <dd>{formatarMoeda(pedido.contrato.valorCredito)}</dd>
                  </div>
                </>
              )}
            </dl>
          </div>
        )}

        <Alerta erro={erro} />

        {(emAnalise || podeCancelar) && (
          <div className="acoes">
            {emAnalise && (
              <Link to={`/pedidos/${pedido.id}/editar`} className="botao botao-contorno">Modificar pedido</Link>
            )}
            {podeCancelar && (
              <button type="button" className="botao botao-perigo" onClick={handleCancelar} disabled={ocupado}>
                Cancelar pedido
              </button>
            )}
          </div>
        )}
      </section>

      {ehAgente && emAnalise && (
        <section className="cartao">
          <h2>Avaliação financeira</h2>
          <p className="texto-suave">Aprove para seguir para o contrato ou reprove informando o motivo.</p>
          <label className="formulario">
            Parecer
            <textarea rows={3} maxLength={500} value={parecer} onChange={(e) => setParecer(e.target.value)}
              placeholder="Obrigatório ao reprovar" />
          </label>
          <div className="acoes">
            <button type="button" className="botao botao-perigo" disabled={ocupado}
              onClick={() => handleAvaliar('REPROVAR')}>
              Reprovar
            </button>
            <button type="button" className="botao" disabled={ocupado} onClick={() => handleAvaliar('APROVAR')}>
              Aprovar
            </button>
          </div>
        </section>
      )}

      {ehAgente && aprovado && (
        <section className="cartao">
          <h2>Executar contrato</h2>
          <p className="texto-suave">
            Defina em nome de quem o automóvel fica registrado e, se houver, o crédito concedido por um banco.
          </p>
          <form className="formulario" onSubmit={handleContratar}>
            <label>
              Proprietário do automóvel
              <select value={proprietario} onChange={(e) => setProprietario(e.target.value)}>
                {Object.entries(ROTULO_PROPRIETARIO).map(([valor, rotulo]) => (
                  <option key={valor} value={valor}>{rotulo}</option>
                ))}
              </select>
            </label>

            <label className="opcao">
              <input type="checkbox" checked={comCredito} onChange={(e) => setComCredito(e.target.checked)} />
              Associar contrato de crédito
            </label>

            {comCredito && (
              <div className="grade-campos">
                <label>
                  Banco
                  <select value={bancoId} onChange={(e) => setBancoId(e.target.value)} required>
                    <option value="" disabled>Selecione...</option>
                    {bancos.map((banco) => (
                      <option key={banco.id} value={banco.id}>{banco.nome}</option>
                    ))}
                  </select>
                </label>
                <label>
                  Valor do crédito (R$)
                  <input type="number" min="0.01" step="0.01" value={valorCredito}
                    onChange={(e) => setValorCredito(e.target.value)} required />
                </label>
              </div>
            )}

            <div className="acoes">
              <button type="submit" className="botao" disabled={ocupado}>Executar contrato</button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}

export default PedidoDetalhe;

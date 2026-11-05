import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Alerta from '../components/Alerta';
import { api } from '../services/api';
import { descreverAutomovel, hojeIso } from '../utils/formatadores';

// Tela única para introduzir (/pedidos/novo) e modificar (/pedidos/:id/editar) um pedido.
function PedidoForm() {
  const { id } = useParams();
  const editando = Boolean(id);
  const navigate = useNavigate();

  const [automoveis, setAutomoveis] = useState([]);
  const [automovelId, setAutomovelId] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try {
        const lista = await api.listarAutomoveis();
        if (!ativo) return;
        setAutomoveis(lista);
        if (editando) {
          const pedido = await api.buscarPedido(id);
          if (!ativo) return;
          setAutomovelId(String(pedido.automovel.id));
          setDataInicio(pedido.dataInicio);
          setDataFim(pedido.dataFim);
        }
      } catch (e) {
        if (ativo) setErro(e);
      } finally {
        if (ativo) setCarregando(false);
      }
    }
    carregar();
    return () => {
      ativo = false;
    };
  }, [id, editando]);

  async function handleSubmit(evento) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    const dados = { automovelId: Number(automovelId), dataInicio, dataFim };
    try {
      const pedido = editando ? await api.atualizarPedido(id, dados) : await api.criarPedido(dados);
      navigate(`/pedidos/${pedido.id}`);
    } catch (e) {
      setErro(e);
      setEnviando(false);
    }
  }

  if (carregando) {
    return <main className="pagina"><p className="texto-suave">Carregando...</p></main>;
  }

  return (
    <main className="pagina pagina-estreita">
      <div className="cartao">
        <h1>{editando ? `Modificar pedido #${id}` : 'Novo pedido de aluguel'}</h1>
        <p className="texto-suave">
          {editando
            ? 'Só é possível modificar pedidos que ainda estão em análise.'
            : 'Escolha o automóvel e o período. O pedido será enviado para análise financeira.'}
        </p>

        <form className="formulario" onSubmit={handleSubmit}>
          <label>
            Automóvel
            <select value={automovelId} onChange={(e) => setAutomovelId(e.target.value)} required>
              <option value="" disabled>Selecione...</option>
              {automoveis.map((automovel) => (
                <option key={automovel.id} value={automovel.id}>{descreverAutomovel(automovel)}</option>
              ))}
            </select>
          </label>

          <div className="grade-campos">
            <label>
              Início
              <input type="date" value={dataInicio} min={editando ? undefined : hojeIso()}
                onChange={(e) => setDataInicio(e.target.value)} required />
            </label>
            <label>
              Término
              <input type="date" value={dataFim} min={dataInicio || hojeIso()}
                onChange={(e) => setDataFim(e.target.value)} required />
            </label>
          </div>

          <Alerta erro={erro} />

          <div className="acoes">
            <Link to={editando ? `/pedidos/${id}` : '/pedidos'} className="botao botao-contorno">Voltar</Link>
            <button type="submit" className="botao" disabled={enviando}>
              {enviando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Enviar pedido'}
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}

export default PedidoForm;

import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Alerta from '../components/Alerta';
import ClienteForm from '../components/ClienteForm';
import { api } from '../services/api';

// Edição de um cliente feita por um agente.
function ClienteEditar() {
  const { id } = useParams();
  const [cliente, setCliente] = useState(null);
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [salvo, setSalvo] = useState(false);

  useEffect(() => {
    api.buscarCliente(id).then(setCliente).catch(setErro);
  }, [id]);

  async function handleSalvar(dados) {
    setErro(null);
    setSalvo(false);
    setEnviando(true);
    try {
      setCliente(await api.atualizarCliente(id, dados));
      setSalvo(true);
    } catch (e) {
      setErro(e);
    } finally {
      setEnviando(false);
    }
  }

  if (!cliente) {
    return (
      <main className="pagina">
        <Alerta erro={erro} />
        {!erro && <p className="texto-suave">Carregando...</p>}
        <Link to="/clientes" className="botao botao-contorno">Voltar</Link>
      </main>
    );
  }

  return (
    <main className="pagina pagina-media">
      <Link to="/clientes" className="voltar">&larr; Clientes</Link>
      <div className="cartao">
        <h1>{cliente.nome}</h1>
        <p className="texto-suave">Usuário: <strong>{cliente.login}</strong></p>
        <ClienteForm key={cliente.id} inicial={cliente} rotuloBotao="Salvar alterações" onSubmit={handleSalvar}
          enviando={enviando} erro={erro} />
        {salvo && <Alerta tipo="sucesso">Dados atualizados com sucesso.</Alerta>}
      </div>
    </main>
  );
}

export default ClienteEditar;

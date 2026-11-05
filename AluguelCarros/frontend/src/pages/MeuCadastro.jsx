import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Alerta from '../components/Alerta';
import ClienteForm from '../components/ClienteForm';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';

// "Meu cadastro": o cliente consulta, modifica e exclui os próprios dados.
function MeuCadastro() {
  const { sair } = useAuth();
  const navigate = useNavigate();

  const [cliente, setCliente] = useState(null);
  const [erro, setErro] = useState(null);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [salvo, setSalvo] = useState(false);

  useEffect(() => {
    api.meuCadastro().then(setCliente).catch(setErro);
  }, []);

  async function handleSalvar(dados) {
    setErro(null);
    setSalvo(false);
    setEnviando(true);
    try {
      setCliente(await api.atualizarCliente(cliente.id, dados));
      setSalvo(true);
    } catch (e) {
      setErro(e);
    } finally {
      setEnviando(false);
    }
  }

  async function handleExcluir() {
    if (!window.confirm('Excluir seu cadastro? Esta ação não pode ser desfeita.')) return;
    setErroExclusao(null);
    try {
      await api.excluirCliente(cliente.id);
      sair();
      navigate('/login');
    } catch (e) {
      setErroExclusao(e);
    }
  }

  if (!cliente) {
    return (
      <main className="pagina">
        <Alerta erro={erro} />
        {!erro && <p className="texto-suave">Carregando...</p>}
      </main>
    );
  }

  return (
    <main className="pagina pagina-media">
      <div className="cartao">
        <h1>Meu cadastro</h1>
        <p className="texto-suave">Usuário: <strong>{cliente.login}</strong></p>
        <ClienteForm key={cliente.id} inicial={cliente} rotuloBotao="Salvar alterações" onSubmit={handleSalvar}
          enviando={enviando} erro={erro} />
        {salvo && <Alerta tipo="sucesso">Dados atualizados com sucesso.</Alerta>}
      </div>

      <div className="cartao zona-perigo">
        <h2>Excluir cadastro</h2>
        <p className="texto-suave">
          Remove seus dados e seu acesso. Só é possível se você não tiver pedidos em análise, aprovados ou contratados.
        </p>
        <Alerta erro={erroExclusao} />
        <button type="button" className="botao botao-perigo" onClick={handleExcluir}>Excluir meu cadastro</button>
      </div>
    </main>
  );
}

export default MeuCadastro;

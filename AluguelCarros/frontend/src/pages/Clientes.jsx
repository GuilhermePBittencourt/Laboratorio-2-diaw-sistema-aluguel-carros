import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../components/Alerta';
import { api } from '../services/api';
import { formatarCpf, formatarMoeda } from '../utils/formatadores';

// Lista de clientes para os agentes (consultar, modificar e excluir).
function Clientes() {
  const [clientes, setClientes] = useState(null);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    api.listarClientes().then(setClientes).catch(setErro);
  }, []);

  async function handleExcluir(cliente) {
    if (!window.confirm(`Excluir o cliente ${cliente.nome}?`)) return;
    setErro(null);
    try {
      await api.excluirCliente(cliente.id);
      setClientes((atuais) => atuais.filter((c) => c.id !== cliente.id));
    } catch (e) {
      setErro(e);
    }
  }

  const rendaTotal = (cliente) => cliente.rendimentos.reduce((soma, r) => soma + Number(r.valor), 0);

  return (
    <main className="pagina">
      <div className="cabecalho-pagina">
        <div>
          <h1>Clientes</h1>
          <p className="texto-suave">Clientes cadastrados no sistema.</p>
        </div>
      </div>

      <Alerta erro={erro} />
      {clientes === null && !erro && <p className="texto-suave">Carregando clientes...</p>}
      {clientes?.length === 0 && <div className="vazio"><p>Nenhum cliente cadastrado.</p></div>}

      {clientes?.length > 0 && (
        <div className="tabela-rolavel">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>Profissão</th>
                <th>Renda mensal</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr key={cliente.id}>
                  <td>{cliente.nome}</td>
                  <td>{formatarCpf(cliente.cpf)}</td>
                  <td>{cliente.profissao}</td>
                  <td>{cliente.rendimentos.length ? formatarMoeda(rendaTotal(cliente)) : '—'}</td>
                  <td className="coluna-acoes">
                    <Link to={`/clientes/${cliente.id}`} className="botao botao-contorno botao-pequeno">Editar</Link>
                    <button type="button" className="botao botao-perigo botao-pequeno"
                      onClick={() => handleExcluir(cliente)}>
                      Excluir
                    </button>
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

export default Clientes;

import { useEffect, useState } from 'react';
import Alerta from '../components/Alerta';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { ROTULO_PROPRIETARIO } from '../utils/formatadores';

const FORMULARIO_VAZIO = { matricula: '', ano: '', marca: '', modelo: '', placa: '', proprietario: 'EMPRESA' };

// Catálogo de automóveis. Agentes também podem cadastrar novos.
function Automoveis() {
  const { ehAgente } = useAuth();
  const [automoveis, setAutomoveis] = useState(null);
  const [erro, setErro] = useState(null);
  const [form, setForm] = useState(FORMULARIO_VAZIO);
  const [erroForm, setErroForm] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    api.listarAutomoveis().then(setAutomoveis).catch(setErro);
  }, []);

  function alterar(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }));
  }

  async function handleSubmit(evento) {
    evento.preventDefault();
    setErroForm(null);
    setEnviando(true);
    try {
      const novo = await api.cadastrarAutomovel({ ...form, ano: Number(form.ano) });
      setAutomoveis((atuais) => [...atuais, novo]);
      setForm(FORMULARIO_VAZIO);
    } catch (e) {
      setErroForm(e);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="pagina">
      <div className="cabecalho-pagina">
        <div>
          <h1>Automóveis</h1>
          <p className="texto-suave">Veículos disponíveis para aluguel.</p>
        </div>
      </div>

      <Alerta erro={erro} />
      {automoveis === null && !erro && <p className="texto-suave">Carregando automóveis...</p>}

      {automoveis?.length > 0 && (
        <div className="tabela-rolavel">
          <table className="tabela">
            <thead>
              <tr>
                <th>Marca</th>
                <th>Modelo</th>
                <th>Ano</th>
                <th>Placa</th>
                <th>Matrícula</th>
                <th>Registrado em nome de</th>
              </tr>
            </thead>
            <tbody>
              {automoveis.map((automovel) => (
                <tr key={automovel.id}>
                  <td>{automovel.marca}</td>
                  <td>{automovel.modelo}</td>
                  <td>{automovel.ano}</td>
                  <td>{automovel.placa}</td>
                  <td>{automovel.matricula}</td>
                  <td>{ROTULO_PROPRIETARIO[automovel.proprietario]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {ehAgente && (
        <section className="cartao">
          <h2>Cadastrar automóvel</h2>
          <form className="formulario" onSubmit={handleSubmit}>
            <div className="grade-campos">
              <label>
                Marca
                <input value={form.marca} onChange={(e) => alterar('marca', e.target.value)} required maxLength={60} />
              </label>
              <label>
                Modelo
                <input value={form.modelo} onChange={(e) => alterar('modelo', e.target.value)} required maxLength={60} />
              </label>
              <label>
                Ano
                <input type="number" min="1950" max="2100" value={form.ano}
                  onChange={(e) => alterar('ano', e.target.value)} required />
              </label>
              <label>
                Placa
                <input value={form.placa} onChange={(e) => alterar('placa', e.target.value.toUpperCase())} required
                  placeholder="ABC1D23" maxLength={8} />
              </label>
              <label>
                Matrícula
                <input value={form.matricula} onChange={(e) => alterar('matricula', e.target.value)} required
                  maxLength={20} />
              </label>
              <label>
                Registrado em nome de
                <select value={form.proprietario} onChange={(e) => alterar('proprietario', e.target.value)}>
                  {Object.entries(ROTULO_PROPRIETARIO).map(([valor, rotulo]) => (
                    <option key={valor} value={valor}>{rotulo}</option>
                  ))}
                </select>
              </label>
            </div>
            <Alerta erro={erroForm} />
            <div className="acoes">
              <button type="submit" className="botao" disabled={enviando}>
                {enviando ? 'Salvando...' : 'Cadastrar automóvel'}
              </button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}

export default Automoveis;

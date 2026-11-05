import { useState } from 'react';
import Alerta from './Alerta';
import { cpfValido } from '../utils/cpf';

const LIMITE_RENDIMENTOS = 3;
const RENDIMENTO_VAZIO = { entidadeEmpregadora: '', valor: '' };

// Formulário de dados do cliente, usado no cadastro, em "Meu cadastro" e na edição feita por agentes.
// Com comCredenciais, pede também usuário e senha (cadastro) e envia { login, senha, cliente }.
function ClienteForm({ inicial, comCredenciais = false, rotuloBotao, onSubmit, enviando, erro }) {
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [rg, setRg] = useState(inicial?.rg ?? '');
  const [cpf, setCpf] = useState(inicial?.cpf ?? '');
  const [nome, setNome] = useState(inicial?.nome ?? '');
  const [endereco, setEndereco] = useState(inicial?.endereco ?? '');
  const [profissao, setProfissao] = useState(inicial?.profissao ?? '');
  const [rendimentos, setRendimentos] = useState(
    inicial?.rendimentos?.map((r) => ({ entidadeEmpregadora: r.entidadeEmpregadora, valor: String(r.valor) })) ?? [],
  );
  const [erroLocal, setErroLocal] = useState('');

  function alterarRendimento(indice, campo, valor) {
    setRendimentos((atuais) => atuais.map((r, i) => (i === indice ? { ...r, [campo]: valor } : r)));
  }

  function adicionarRendimento() {
    setRendimentos((atuais) => [...atuais, { ...RENDIMENTO_VAZIO }]);
  }

  function removerRendimento(indice) {
    setRendimentos((atuais) => atuais.filter((_, i) => i !== indice));
  }

  function handleSubmit(evento) {
    evento.preventDefault();
    setErroLocal('');

    if (!cpfValido(cpf)) {
      setErroLocal('Informe um CPF válido.');
      return;
    }
    if (rendimentos.some((r) => !r.entidadeEmpregadora.trim() || r.valor === '' || Number(r.valor) < 0)) {
      setErroLocal('Preencha a entidade empregadora e um valor válido em cada rendimento.');
      return;
    }

    const cliente = {
      rg: rg.trim(),
      cpf: cpf.replace(/\D/g, ''),
      nome: nome.trim(),
      endereco: endereco.trim(),
      profissao: profissao.trim(),
      rendimentos: rendimentos.map((r) => ({
        entidadeEmpregadora: r.entidadeEmpregadora.trim(),
        valor: Number(r.valor),
      })),
    };
    onSubmit(comCredenciais ? { login: login.trim(), senha, cliente } : cliente);
  }

  return (
    <form className="formulario" onSubmit={handleSubmit}>
      {comCredenciais && (
        <fieldset>
          <legend>Acesso</legend>
          <div className="grade-campos">
            <label>
              Usuário
              <input value={login} onChange={(e) => setLogin(e.target.value)} required minLength={3}
                maxLength={50} autoComplete="username" />
            </label>
            <label>
              Senha
              <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required
                minLength={8} maxLength={72} autoComplete="new-password" />
              <small>Entre 8 e 72 caracteres.</small>
            </label>
          </div>
        </fieldset>
      )}

      <fieldset>
        <legend>Dados do cliente</legend>
        <div className="grade-campos">
          <label className="campo-largo">
            Nome completo
            <input value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={120} />
          </label>
          <label>
            CPF
            <input value={cpf} onChange={(e) => { setCpf(e.target.value); setErroLocal(''); }} required inputMode="numeric"
              placeholder="000.000.000-00" maxLength={14} />
          </label>
          <label>
            RG
            <input value={rg} onChange={(e) => setRg(e.target.value)} required maxLength={20} />
          </label>
          <label className="campo-largo">
            Endereço
            <input value={endereco} onChange={(e) => setEndereco(e.target.value)} required maxLength={255} />
          </label>
          <label>
            Profissão
            <input value={profissao} onChange={(e) => setProfissao(e.target.value)} required maxLength={80} />
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Rendimentos (até {LIMITE_RENDIMENTOS})</legend>
        {rendimentos.length === 0 && <p className="texto-suave">Nenhum rendimento informado.</p>}
        {rendimentos.map((rendimento, indice) => (
          // a lista é pequena e só muda por adição/remoção, então o índice basta como chave
          <div className="linha-rendimento" key={indice}>
            <label>
              Entidade empregadora
              <input value={rendimento.entidadeEmpregadora} maxLength={120}
                onChange={(e) => alterarRendimento(indice, 'entidadeEmpregadora', e.target.value)} />
            </label>
            <label>
              Valor mensal (R$)
              <input type="number" min="0" step="0.01" value={rendimento.valor}
                onChange={(e) => alterarRendimento(indice, 'valor', e.target.value)} />
            </label>
            <button type="button" className="botao botao-contorno botao-pequeno"
              onClick={() => removerRendimento(indice)}>
              Remover
            </button>
          </div>
        ))}
        {rendimentos.length < LIMITE_RENDIMENTOS && (
          <button type="button" className="botao botao-contorno botao-pequeno" onClick={adicionarRendimento}>
            Adicionar rendimento
          </button>
        )}
      </fieldset>

      <Alerta erro={erro} />
      <Alerta>{erroLocal}</Alerta>

      <div className="acoes">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Salvando...' : rotuloBotao}
        </button>
      </div>
    </form>
  );
}

export default ClienteForm;

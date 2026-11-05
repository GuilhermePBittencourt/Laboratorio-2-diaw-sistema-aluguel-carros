// Camada de acesso à API do back-end (Spring Boot).
// Todas as telas passam por aqui: o token JWT é anexado automaticamente.

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const CHAVE_SESSAO = 'aluguel-carros:sessao';

export class ApiError extends Error {
  constructor(status, mensagem, campos = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

// ---------- sessão (token + dados do usuário) ----------

export function lerSessao() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE_SESSAO));
  } catch {
    return null;
  }
}

export function salvarSessao(sessao) {
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
  } catch {
    // sem armazenamento disponível: a sessão vale só até recarregar a página
  }
}

export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch {
    // nada a fazer
  }
}

// ---------- requisição genérica ----------

async function requisicao(caminho, { metodo = 'GET', corpo, autenticado = true } = {}) {
  const cabecalhos = { Accept: 'application/json' };
  if (corpo !== undefined) {
    cabecalhos['Content-Type'] = 'application/json';
  }
  const sessao = lerSessao();
  if (autenticado && sessao?.token) {
    cabecalhos.Authorization = `Bearer ${sessao.token}`;
  }

  let resposta;
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se o back-end está em execução.');
  }

  if (resposta.status === 204) {
    return null;
  }

  const dados = await resposta.json().catch(() => null);

  if (!resposta.ok) {
    if (resposta.status === 401 && autenticado) {
      // token expirado ou inválido: a tela de login assume
      limparSessao();
      window.dispatchEvent(new Event('sessao-expirada'));
    }
    throw new ApiError(
      resposta.status,
      dados?.mensagem ?? 'Ocorreu um erro inesperado. Tente novamente.',
      dados?.campos ?? {},
    );
  }
  return dados;
}

// ---------- endpoints ----------

export const api = {
  // autenticação
  login: (login, senha) =>
    requisicao('/api/auth/login', { metodo: 'POST', corpo: { login, senha }, autenticado: false }),
  registrar: (dados) =>
    requisicao('/api/auth/registro', { metodo: 'POST', corpo: dados, autenticado: false }),

  // clientes
  meuCadastro: () => requisicao('/api/clientes/me'),
  listarClientes: () => requisicao('/api/clientes'),
  buscarCliente: (id) => requisicao(`/api/clientes/${id}`),
  atualizarCliente: (id, dados) => requisicao(`/api/clientes/${id}`, { metodo: 'PUT', corpo: dados }),
  excluirCliente: (id) => requisicao(`/api/clientes/${id}`, { metodo: 'DELETE' }),

  // automóveis e agentes
  listarAutomoveis: () => requisicao('/api/automoveis'),
  cadastrarAutomovel: (dados) => requisicao('/api/automoveis', { metodo: 'POST', corpo: dados }),
  listarAgentes: (tipo) => requisicao(`/api/agentes${tipo ? `?tipo=${tipo}` : ''}`),

  // pedidos
  listarPedidos: (status) => requisicao(`/api/pedidos${status ? `?status=${status}` : ''}`),
  buscarPedido: (id) => requisicao(`/api/pedidos/${id}`),
  criarPedido: (dados) => requisicao('/api/pedidos', { metodo: 'POST', corpo: dados }),
  atualizarPedido: (id, dados) => requisicao(`/api/pedidos/${id}`, { metodo: 'PUT', corpo: dados }),
  cancelarPedido: (id) => requisicao(`/api/pedidos/${id}/cancelamento`, { metodo: 'POST' }),
  avaliarPedido: (id, dados) => requisicao(`/api/pedidos/${id}/avaliacao`, { metodo: 'POST', corpo: dados }),
  contratarPedido: (id, dados) => requisicao(`/api/pedidos/${id}/contrato`, { metodo: 'POST', corpo: dados }),
};

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, lerSessao, limparSessao, salvarSessao } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [sessao, setSessao] = useState(() => lerSessao());

  const sair = useCallback(() => {
    limparSessao();
    setSessao(null);
  }, []);

  // O serviço de API avisa quando o servidor recusa o token (expirado ou inválido).
  useEffect(() => {
    window.addEventListener('sessao-expirada', sair);
    return () => window.removeEventListener('sessao-expirada', sair);
  }, [sair]);

  const iniciarSessao = useCallback((resposta) => {
    salvarSessao(resposta);
    setSessao(resposta);
  }, []);

  const entrar = useCallback(
    async (login, senha) => iniciarSessao(await api.login(login, senha)),
    [iniciarSessao],
  );

  const registrar = useCallback(
    async (dados) => iniciarSessao(await api.registrar(dados)),
    [iniciarSessao],
  );

  const valor = useMemo(
    () => ({
      sessao,
      nome: sessao?.nome ?? '',
      perfil: sessao?.perfil ?? null,
      autenticado: Boolean(sessao),
      ehCliente: sessao?.perfil === 'CLIENTE',
      ehAgente: sessao?.perfil === 'AGENTE',
      entrar,
      registrar,
      sair,
    }),
    [sessao, entrar, registrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error('useAuth precisa estar dentro de <AuthProvider>');
  }
  return contexto;
}

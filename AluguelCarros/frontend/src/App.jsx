import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Header from './components/Header';
import RotaProtegida from './components/RotaProtegida';
import { AuthProvider } from './context/AuthContext';
import Automoveis from './pages/Automoveis';
import ClienteEditar from './pages/ClienteEditar';
import Clientes from './pages/Clientes';
import Login from './pages/Login';
import MeuCadastro from './pages/MeuCadastro';
import PedidoDetalhe from './pages/PedidoDetalhe';
import PedidoForm from './pages/PedidoForm';
import Pedidos from './pages/Pedidos';
import Registro from './pages/Registro';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Header />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />

          {/* qualquer usuário autenticado */}
          <Route element={<RotaProtegida />}>
            <Route path="/pedidos" element={<Pedidos />} />
            <Route path="/pedidos/:id" element={<PedidoDetalhe />} />
            <Route path="/automoveis" element={<Automoveis />} />
          </Route>

          {/* só clientes */}
          <Route element={<RotaProtegida perfis={['CLIENTE']} />}>
            <Route path="/pedidos/novo" element={<PedidoForm />} />
            <Route path="/perfil" element={<MeuCadastro />} />
          </Route>

          {/* clientes e agentes podem modificar um pedido em análise */}
          <Route element={<RotaProtegida />}>
            <Route path="/pedidos/:id/editar" element={<PedidoForm />} />
          </Route>

          {/* só agentes */}
          <Route element={<RotaProtegida perfis={['AGENTE']} />}>
            <Route path="/clientes" element={<Clientes />} />
            <Route path="/clientes/:id" element={<ClienteEditar />} />
          </Route>

          <Route path="/" element={<Navigate to="/pedidos" replace />} />
          <Route path="*" element={<Navigate to="/pedidos" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

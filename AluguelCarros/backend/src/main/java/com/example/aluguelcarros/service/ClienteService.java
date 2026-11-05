package com.example.aluguelcarros.service;

import com.example.aluguelcarros.dto.ClienteRequest;
import com.example.aluguelcarros.dto.ClienteResponse;
import com.example.aluguelcarros.dto.RendimentoDto;
import com.example.aluguelcarros.exception.ConflitoException;
import com.example.aluguelcarros.exception.RecursoNaoEncontradoException;
import com.example.aluguelcarros.model.Cliente;
import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.Rendimento;
import com.example.aluguelcarros.model.StatusPedido;
import com.example.aluguelcarros.model.Usuario;
import com.example.aluguelcarros.repository.ClienteRepository;
import com.example.aluguelcarros.repository.PedidoRepository;
import com.example.aluguelcarros.repository.UsuarioRepository;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import com.example.aluguelcarros.validation.CpfValidator;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras do CRUD de clientes. O "create" acontece no cadastro (AuthService.registrar). */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                          PedidoRepository pedidoRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente cadastrar(String login, String senha, ClienteRequest dados) {
        if (usuarioRepository.existsByLogin(login)) {
            throw new ConflitoException("Este usuário já está em uso.");
        }
        String cpf = CpfValidator.somenteDigitos(dados.cpf());
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um cliente cadastrado com este CPF.");
        }

        Usuario usuario = usuarioRepository.save(new Usuario(login, passwordEncoder.encode(senha), Perfil.CLIENTE));
        Cliente cliente = new Cliente(usuario, dados.rg().trim(), cpf, dados.nome().trim(),
                dados.endereco().trim(), dados.profissao().trim(), converterRendimentos(dados.rendimentos()));
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNomeAsc().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, UsuarioAutenticado solicitante) {
        return ClienteResponse.de(buscarComAcesso(id, solicitante));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarDoUsuario(UsuarioAutenticado solicitante) {
        Cliente cliente = clienteRepository.findByUsuarioId(solicitante.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de cliente não encontrado."));
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest dados, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarComAcesso(id, solicitante);

        String cpf = CpfValidator.somenteDigitos(dados.cpf());
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoException("Já existe outro cliente cadastrado com este CPF.");
        }

        cliente.atualizarDados(dados.rg().trim(), cpf, dados.nome().trim(), dados.endereco().trim(),
                dados.profissao().trim(), converterRendimentos(dados.rendimentos()));
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarComAcesso(id, solicitante);

        if (pedidoRepository.existsByClienteIdAndStatusIn(id, StatusPedido.ativos())) {
            throw new ConflitoException("Não é possível excluir o cliente: ele possui pedidos ativos. Cancele-os antes.");
        }

        // Pedidos já encerrados (cancelados ou reprovados) deixam de fazer sentido sem o cliente.
        pedidoRepository.deleteByClienteId(id);
        Usuario usuario = cliente.getUsuario();
        clienteRepository.delete(cliente);
        clienteRepository.flush(); // o cliente precisa sair antes do usuário (chave estrangeira)
        usuarioRepository.delete(usuario);
    }

    /** Um cliente só enxerga o próprio cadastro; agentes enxergam todos. Para os demais, o cadastro "não existe". */
    private Cliente buscarComAcesso(Long id, UsuarioAutenticado solicitante) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        boolean ehDono = cliente.getUsuario().getId().equals(solicitante.id());
        if (solicitante.ehCliente() && !ehDono) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado.");
        }
        return cliente;
    }

    private List<Rendimento> converterRendimentos(List<RendimentoDto> rendimentos) {
        return rendimentos.stream().map(RendimentoDto::paraEntidade).toList();
    }
}

package com.example.aluguelcarros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aluguelcarros.dto.ClienteRequest;
import com.example.aluguelcarros.dto.RendimentoDto;
import com.example.aluguelcarros.exception.ConflitoException;
import com.example.aluguelcarros.exception.RecursoNaoEncontradoException;
import com.example.aluguelcarros.model.Cliente;
import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.StatusPedido;
import com.example.aluguelcarros.model.Usuario;
import com.example.aluguelcarros.repository.ClienteRepository;
import com.example.aluguelcarros.repository.PedidoRepository;
import com.example.aluguelcarros.repository.UsuarioRepository;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ClienteService service;

    private final UsuarioAutenticado maria = new UsuarioAutenticado(10L, "maria", Perfil.CLIENTE);
    private final UsuarioAutenticado agente = new UsuarioAutenticado(20L, "agente.empresa", Perfil.AGENTE);

    @BeforeEach
    void preparar() {
        service = new ClienteService(clienteRepository, usuarioRepository, pedidoRepository, passwordEncoder);
    }

    private ClienteRequest dados(String cpf) {
        return new ClienteRequest(" MG-123 ", cpf, " Maria Souza ", "Rua A, 10", "Engenheira",
                List.of(new RendimentoDto("Empresa X", new BigDecimal("5000.00"))));
    }

    private Cliente clienteSalvo() {
        Usuario usuario = new Usuario("maria", "hash", Perfil.CLIENTE);
        ReflectionTestUtils.setField(usuario, "id", 10L);
        Cliente cliente = new Cliente(usuario, "MG-123", "52998224725", "Maria Souza", "Rua A, 10", "Engenheira", List.of());
        ReflectionTestUtils.setField(cliente, "id", 1L);
        return cliente;
    }

    @Test
    void cadastraClienteGuardandoOCpfSoComDigitosEASenhaCriptografada() {
        when(usuarioRepository.existsByLogin("maria")).thenReturn(false);
        when(clienteRepository.existsByCpf("52998224725")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente cliente = service.cadastrar("maria", "senha1234", dados("529.982.247-25"));

        assertEquals("52998224725", cliente.getCpf());
        assertEquals("MG-123", cliente.getRg());
        assertEquals("Maria Souza", cliente.getNome());
        assertEquals("hash-bcrypt", cliente.getUsuario().getSenhaHash());
        assertEquals(1, cliente.getRendimentos().size());
    }

    @Test
    void naoCadastraLoginRepetido() {
        when(usuarioRepository.existsByLogin("maria")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.cadastrar("maria", "senha1234", dados("529.982.247-25")));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void naoCadastraCpfRepetido() {
        when(usuarioRepository.existsByLogin("maria")).thenReturn(false);
        when(clienteRepository.existsByCpf("52998224725")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.cadastrar("maria", "senha1234", dados("529.982.247-25")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void clienteNaoEnxergaCadastroDeOutroCliente() {
        Cliente deOutro = clienteSalvo();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(deOutro));
        UsuarioAutenticado intruso = new UsuarioAutenticado(99L, "joao", Perfil.CLIENTE);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(1L, intruso));
    }

    @Test
    void agenteEnxergaCadastroDeQualquerCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo()));

        assertEquals("Maria Souza", service.buscar(1L, agente).nome());
    }

    @Test
    void atualizaOsDadosDoProprioCadastro() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo()));
        when(clienteRepository.existsByCpfAndIdNot("11144477735", 1L)).thenReturn(false);

        var resposta = service.atualizar(1L, dados("111.444.777-35"), maria);

        assertEquals("11144477735", resposta.cpf());
        assertEquals(1, resposta.rendimentos().size());
    }

    @Test
    void naoAtualizaParaUmCpfQueJaPertenceAOutroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo()));
        when(clienteRepository.existsByCpfAndIdNot("11144477735", 1L)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.atualizar(1L, dados("111.444.777-35"), maria));
    }

    @Test
    void naoExcluiClienteComPedidosAtivos() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo()));
        when(pedidoRepository.existsByClienteIdAndStatusIn(1L, StatusPedido.ativos())).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.excluir(1L, maria));
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void excluiClienteSemPedidosAtivosJuntoComOUsuario() {
        Cliente cliente = clienteSalvo();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.existsByClienteIdAndStatusIn(1L, StatusPedido.ativos())).thenReturn(false);

        service.excluir(1L, maria);

        verify(pedidoRepository).deleteByClienteId(1L);
        verify(clienteRepository).delete(cliente);
        verify(usuarioRepository).delete(cliente.getUsuario());
    }
}

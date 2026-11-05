package com.example.aluguelcarros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aluguelcarros.dto.AvaliacaoRequest;
import com.example.aluguelcarros.dto.ContratoRequest;
import com.example.aluguelcarros.dto.PedidoRequest;
import com.example.aluguelcarros.dto.PedidoResponse;
import com.example.aluguelcarros.exception.AcessoNegadoException;
import com.example.aluguelcarros.exception.ConflitoException;
import com.example.aluguelcarros.exception.RecursoNaoEncontradoException;
import com.example.aluguelcarros.exception.RequisicaoInvalidaException;
import com.example.aluguelcarros.model.Agente;
import com.example.aluguelcarros.model.Automovel;
import com.example.aluguelcarros.model.Cliente;
import com.example.aluguelcarros.model.Contrato;
import com.example.aluguelcarros.model.Decisao;
import com.example.aluguelcarros.model.PedidoAluguel;
import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.StatusPedido;
import com.example.aluguelcarros.model.TipoAgente;
import com.example.aluguelcarros.model.TipoProprietario;
import com.example.aluguelcarros.model.Usuario;
import com.example.aluguelcarros.repository.AgenteRepository;
import com.example.aluguelcarros.repository.AutomovelRepository;
import com.example.aluguelcarros.repository.ClienteRepository;
import com.example.aluguelcarros.repository.ContratoRepository;
import com.example.aluguelcarros.repository.PedidoRepository;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private AgenteRepository agenteRepository;
    @Mock
    private AutomovelRepository automovelRepository;
    @Mock
    private ContratoRepository contratoRepository;

    private PedidoService service;

    private final UsuarioAutenticado clienteLogado = new UsuarioAutenticado(10L, "maria", Perfil.CLIENTE);
    private final UsuarioAutenticado agenteLogado = new UsuarioAutenticado(20L, "agente.banco", Perfil.AGENTE);

    private LocalDate hoje;
    private Cliente cliente;
    private Automovel automovel;
    private Agente agente;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-11-05T15:00:00Z"), FUSO);
        hoje = LocalDate.now(relogio);
        service = new PedidoService(pedidoRepository, clienteRepository, agenteRepository, automovelRepository,
                contratoRepository, relogio);

        Usuario usuarioCliente = comId(new Usuario("maria", "hash", Perfil.CLIENTE), 10L);
        cliente = comId(new Cliente(usuarioCliente, "MG-1", "52998224725", "Maria", "Rua A", "Engenheira", List.of()), 1L);
        automovel = comId(new Automovel("MAT-1", 2023, "Fiat", "Argo", "ABC1D23", TipoProprietario.EMPRESA), 5L);
        Usuario usuarioAgente = comId(new Usuario("agente.banco", "hash", Perfil.AGENTE), 20L);
        agente = comId(new Agente(usuarioAgente, "Banco Horizonte", TipoAgente.BANCO), 2L);
    }

    private static <T> T comId(T entidade, Long id) {
        ReflectionTestUtils.setField(entidade, "id", id);
        return entidade;
    }

    private PedidoAluguel pedidoEmAnalise() {
        return comId(new PedidoAluguel(cliente, automovel, hoje.plusDays(1), hoje.plusDays(4)), 100L);
    }

    // ---------- criar ----------

    @Test
    void criaPedidoEmAnaliseQuandoOsDadosSaoValidos() {
        LocalDate inicio = hoje.plusDays(1);
        LocalDate fim = hoje.plusDays(4);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(Optional.of(cliente));
        when(automovelRepository.findById(5L)).thenReturn(Optional.of(automovel));
        when(pedidoRepository.existeConflitoDePeriodo(eq(5L), eq(inicio), eq(fim), eq(StatusPedido.ativos()), eq(0L)))
                .thenReturn(false);
        when(pedidoRepository.save(any(PedidoAluguel.class))).thenAnswer(chamada -> chamada.getArgument(0));

        PedidoResponse resposta = service.criar(new PedidoRequest(5L, inicio, fim), clienteLogado);

        assertEquals(StatusPedido.EM_ANALISE, resposta.status());
        assertEquals("Maria", resposta.clienteNome());
        assertEquals("ABC1D23", resposta.automovel().placa());
    }

    @Test
    void naoCriaPedidoComInicioNoPassado() {
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(Optional.of(cliente));
        when(automovelRepository.findById(5L)).thenReturn(Optional.of(automovel));

        assertThrows(RequisicaoInvalidaException.class,
                () -> service.criar(new PedidoRequest(5L, hoje.minusDays(1), hoje.plusDays(2)), clienteLogado));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void naoCriaPedidoComTerminoAntesDoInicio() {
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(Optional.of(cliente));
        when(automovelRepository.findById(5L)).thenReturn(Optional.of(automovel));

        assertThrows(RequisicaoInvalidaException.class,
                () -> service.criar(new PedidoRequest(5L, hoje.plusDays(5), hoje.plusDays(5)), clienteLogado));
    }

    @Test
    void naoCriaPedidoQuandoOAutomovelJaEstaReservadoNoPeriodo() {
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(Optional.of(cliente));
        when(automovelRepository.findById(5L)).thenReturn(Optional.of(automovel));
        when(pedidoRepository.existeConflitoDePeriodo(eq(5L), any(LocalDate.class), any(LocalDate.class),
                eq(StatusPedido.ativos()), eq(0L))).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.criar(new PedidoRequest(5L, hoje.plusDays(1), hoje.plusDays(3)), clienteLogado));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void agenteNaoCriaPedido() {
        assertThrows(AcessoNegadoException.class,
                () -> service.criar(new PedidoRequest(5L, hoje.plusDays(1), hoje.plusDays(3)), agenteLogado));
    }

    // ---------- consulta ----------

    @Test
    void clienteNaoEnxergaPedidoDeOutroCliente() {
        Usuario outroUsuario = comId(new Usuario("joao", "hash", Perfil.CLIENTE), 99L);
        Cliente outro = comId(new Cliente(outroUsuario, "MG-2", "11144477735", "João", "Rua B", "Médico", List.of()), 3L);
        PedidoAluguel pedidoDoOutro = comId(new PedidoAluguel(outro, automovel, hoje.plusDays(1), hoje.plusDays(2)), 200L);
        when(pedidoRepository.findById(200L)).thenReturn(Optional.of(pedidoDoOutro));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(200L, clienteLogado));
    }

    @Test
    void agenteEnxergaQualquerPedido() {
        PedidoAluguel pedido = pedidoEmAnalise();
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(contratoRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

        PedidoResponse resposta = service.buscar(100L, agenteLogado);

        assertEquals(100L, resposta.id());
    }

    // ---------- modificar e cancelar ----------

    @Test
    void clienteModificaPedidoEmAnalise() {
        PedidoAluguel pedido = pedidoEmAnalise();
        LocalDate novoInicio = hoje.plusDays(10);
        LocalDate novoFim = hoje.plusDays(12);
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(automovelRepository.findById(5L)).thenReturn(Optional.of(automovel));
        when(pedidoRepository.existeConflitoDePeriodo(eq(5L), eq(novoInicio), eq(novoFim),
                eq(StatusPedido.ativos()), eq(100L))).thenReturn(false);
        when(contratoRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

        PedidoResponse resposta = service.atualizar(100L, new PedidoRequest(5L, novoInicio, novoFim), clienteLogado);

        assertEquals(novoInicio, resposta.dataInicio());
        assertEquals(novoFim, resposta.dataFim());
    }

    @Test
    void naoModificaPedidoJaAprovado() {
        PedidoAluguel pedido = pedidoEmAnalise();
        pedido.avaliar(agente, Decisao.APROVAR, null);
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));

        assertThrows(ConflitoException.class,
                () -> service.atualizar(100L, new PedidoRequest(5L, hoje.plusDays(2), hoje.plusDays(3)), clienteLogado));
    }

    @Test
    void clienteCancelaOProprioPedido() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoEmAnalise()));
        when(contratoRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

        PedidoResponse resposta = service.cancelar(100L, clienteLogado);

        assertEquals(StatusPedido.CANCELADO, resposta.status());
    }

    @Test
    void agenteNaoCancelaPedido() {
        assertThrows(AcessoNegadoException.class, () -> service.cancelar(100L, agenteLogado));
    }

    // ---------- avaliação ----------

    @Test
    void agenteAprovaPedidoEmAnalise() {
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoEmAnalise()));
        when(contratoRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

        PedidoResponse resposta = service.avaliar(100L, new AvaliacaoRequest(Decisao.APROVAR, "Renda compatível"), agenteLogado);

        assertEquals(StatusPedido.APROVADO, resposta.status());
        assertEquals("Banco Horizonte", resposta.avaliadoPor());
        assertEquals("Renda compatível", resposta.parecer());
    }

    @Test
    void reprovarExigeParecer() {
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));

        assertThrows(RequisicaoInvalidaException.class,
                () -> service.avaliar(100L, new AvaliacaoRequest(Decisao.REPROVAR, "  "), agenteLogado));
    }

    @Test
    void clienteNaoAvaliaPedido() {
        assertThrows(AcessoNegadoException.class,
                () -> service.avaliar(100L, new AvaliacaoRequest(Decisao.APROVAR, null), clienteLogado));
    }

    // ---------- contrato ----------

    @Test
    void agenteExecutaContratoComCreditoDeUmBanco() {
        PedidoAluguel pedido = pedidoEmAnalise();
        pedido.avaliar(agente, Decisao.APROVAR, null);
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));
        when(agenteRepository.findById(2L)).thenReturn(Optional.of(agente));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(chamada -> chamada.getArgument(0));

        PedidoResponse resposta = service.contratar(100L,
                new ContratoRequest(TipoProprietario.BANCO, 2L, new BigDecimal("15000.00")), agenteLogado);

        assertEquals(StatusPedido.CONTRATADO, resposta.status());
        assertEquals(TipoProprietario.BANCO, automovel.getProprietario());
        assertEquals("Banco Horizonte", resposta.contrato().bancoCredito());
    }

    @Test
    void naoContrataPedidoQueAindaEstaEmAnalise() {
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoEmAnalise()));

        assertThrows(IllegalStateException.class,
                () -> service.contratar(100L, new ContratoRequest(TipoProprietario.EMPRESA, null, null), agenteLogado));
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void creditoPrecisaDeBancoEValorJuntos() {
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));

        assertThrows(RequisicaoInvalidaException.class,
                () -> service.contratar(100L, new ContratoRequest(TipoProprietario.BANCO, 2L, null), agenteLogado));
    }

    @Test
    void creditoSoPodeVirDeUmBanco() {
        Agente empresa = comId(new Agente(comId(new Usuario("empresa", "hash", Perfil.AGENTE), 30L),
                "Locadora Estrada Livre", TipoAgente.EMPRESA), 3L);
        when(agenteRepository.findByUsuarioId(20L)).thenReturn(Optional.of(agente));
        when(agenteRepository.findById(3L)).thenReturn(Optional.of(empresa));

        assertThrows(RequisicaoInvalidaException.class,
                () -> service.contratar(100L, new ContratoRequest(TipoProprietario.EMPRESA, 3L, new BigDecimal("1000")), agenteLogado));
    }
}

package com.example.aluguelcarros.service;

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
import com.example.aluguelcarros.model.StatusPedido;
import com.example.aluguelcarros.model.TipoAgente;
import com.example.aluguelcarros.repository.AgenteRepository;
import com.example.aluguelcarros.repository.AutomovelRepository;
import com.example.aluguelcarros.repository.ClienteRepository;
import com.example.aluguelcarros.repository.ContratoRepository;
import com.example.aluguelcarros.repository.PedidoRepository;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras dos pedidos de aluguel:
 * o cliente cria, modifica, consulta e cancela; o agente avalia, modifica e executa o contrato.
 */
@Service
public class PedidoService {

    /** Valor usado quando não há pedido a desconsiderar na checagem de conflito de datas. */
    private static final Long SEM_PEDIDO = 0L;

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final AgenteRepository agenteRepository;
    private final AutomovelRepository automovelRepository;
    private final ContratoRepository contratoRepository;
    private final Clock relogio;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
                         AgenteRepository agenteRepository, AutomovelRepository automovelRepository,
                         ContratoRepository contratoRepository, Clock relogio) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.agenteRepository = agenteRepository;
        this.automovelRepository = automovelRepository;
        this.contratoRepository = contratoRepository;
        this.relogio = relogio;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest dados, UsuarioAutenticado solicitante) {
        Cliente cliente = clienteDoSolicitante(solicitante);
        Automovel automovel = buscarAutomovel(dados.automovelId());
        validarPeriodo(dados.dataInicio(), dados.dataFim());
        validarAutomovelLivre(automovel.getId(), dados.dataInicio(), dados.dataFim(), SEM_PEDIDO);

        PedidoAluguel pedido = pedidoRepository.save(
                new PedidoAluguel(cliente, automovel, dados.dataInicio(), dados.dataFim()));
        return PedidoResponse.de(pedido, null);
    }

    /** Cliente: só os próprios pedidos. Agente: todos. Em ambos os casos dá para filtrar pelo status. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioAutenticado solicitante, StatusPedido status) {
        List<PedidoAluguel> pedidos;
        if (solicitante.ehCliente()) {
            Long clienteId = clienteDoSolicitante(solicitante).getId();
            pedidos = status == null
                    ? pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)
                    : pedidoRepository.findByClienteIdAndStatusOrderByCriadoEmDesc(clienteId, status);
        } else {
            pedidos = status == null
                    ? pedidoRepository.findAllByOrderByCriadoEmDesc()
                    : pedidoRepository.findByStatusOrderByCriadoEmDesc(status);
        }
        return responder(pedidos);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, UsuarioAutenticado solicitante) {
        return responder(buscarComAcesso(id, solicitante));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest dados, UsuarioAutenticado solicitante) {
        PedidoAluguel pedido = buscarComAcesso(id, solicitante);
        if (!pedido.getStatus().aceitaAlteracao()) {
            throw new ConflitoException("Só é possível modificar pedidos que estão em análise.");
        }

        Automovel automovel = buscarAutomovel(dados.automovelId());
        validarPeriodo(dados.dataInicio(), dados.dataFim());
        validarAutomovelLivre(automovel.getId(), dados.dataInicio(), dados.dataFim(), pedido.getId());

        pedido.alterar(automovel, dados.dataInicio(), dados.dataFim());
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, UsuarioAutenticado solicitante) {
        if (!solicitante.ehCliente()) {
            throw new AcessoNegadoException("Somente o cliente pode cancelar o próprio pedido.");
        }
        PedidoAluguel pedido = buscarComAcesso(id, solicitante);
        pedido.cancelar();
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse avaliar(Long id, AvaliacaoRequest dados, UsuarioAutenticado solicitante) {
        Agente agente = agenteDoSolicitante(solicitante);
        String parecer = dados.parecer() == null ? "" : dados.parecer().trim();
        if (dados.decisao() == Decisao.REPROVAR && parecer.isEmpty()) {
            throw new RequisicaoInvalidaException("Informe o parecer financeiro ao reprovar um pedido.");
        }

        PedidoAluguel pedido = buscarPedido(id);
        pedido.avaliar(agente, dados.decisao(), parecer.isEmpty() ? null : parecer);
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse contratar(Long id, ContratoRequest dados, UsuarioAutenticado solicitante) {
        Agente agente = agenteDoSolicitante(solicitante);
        Agente banco = buscarBancoDoCredito(dados.bancoCreditoId(), dados.valorCredito());

        PedidoAluguel pedido = buscarPedido(id);
        pedido.marcarComoContratado();
        pedido.getAutomovel().registrarProprietario(dados.proprietario());

        Contrato contrato = contratoRepository.save(
                new Contrato(pedido, dados.proprietario(), agente, banco, dados.valorCredito()));
        return PedidoResponse.de(pedido, contrato);
    }

    // ---------- regras auxiliares ----------

    /** O crédito é opcional, mas banco e valor andam juntos, e quem concede precisa ser um banco. */
    private Agente buscarBancoDoCredito(Long bancoCreditoId, BigDecimal valorCredito) {
        if (bancoCreditoId == null && valorCredito == null) {
            return null;
        }
        if (bancoCreditoId == null || valorCredito == null) {
            throw new RequisicaoInvalidaException("Para associar um crédito, informe o banco e o valor.");
        }
        if (valorCredito.signum() <= 0) {
            throw new RequisicaoInvalidaException("O valor do crédito deve ser maior que zero.");
        }
        Agente banco = agenteRepository.findById(bancoCreditoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Banco não encontrado."));
        if (banco.getTipo() != TipoAgente.BANCO) {
            throw new RequisicaoInvalidaException("O crédito só pode ser concedido por um banco.");
        }
        return banco;
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isBefore(LocalDate.now(relogio))) {
            throw new RequisicaoInvalidaException("A data de início não pode estar no passado.");
        }
        if (!fim.isAfter(inicio)) {
            throw new RequisicaoInvalidaException("A data de término deve ser posterior à data de início.");
        }
    }

    private void validarAutomovelLivre(Long automovelId, LocalDate inicio, LocalDate fim, Long ignorarPedidoId) {
        if (pedidoRepository.existeConflitoDePeriodo(automovelId, inicio, fim, StatusPedido.ativos(), ignorarPedidoId)) {
            throw new ConflitoException("O automóvel já está reservado em parte deste período.");
        }
    }

    private Automovel buscarAutomovel(Long id) {
        return automovelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel não encontrado."));
    }

    private PedidoAluguel buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    /** Cliente só enxerga os próprios pedidos (para ele, os dos outros "não existem"); agente enxerga todos. */
    private PedidoAluguel buscarComAcesso(Long id, UsuarioAutenticado solicitante) {
        PedidoAluguel pedido = buscarPedido(id);
        boolean ehDono = pedido.getCliente().getUsuario().getId().equals(solicitante.id());
        if (solicitante.ehCliente() && !ehDono) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado.");
        }
        return pedido;
    }

    private Cliente clienteDoSolicitante(UsuarioAutenticado solicitante) {
        if (!solicitante.ehCliente()) {
            throw new AcessoNegadoException("Esta operação é exclusiva de clientes.");
        }
        return clienteRepository.findByUsuarioId(solicitante.id())
                .orElseThrow(() -> new AcessoNegadoException("Cadastro de cliente não encontrado."));
    }

    private Agente agenteDoSolicitante(UsuarioAutenticado solicitante) {
        if (!solicitante.ehAgente()) {
            throw new AcessoNegadoException("Esta operação é exclusiva de agentes.");
        }
        return agenteRepository.findByUsuarioId(solicitante.id())
                .orElseThrow(() -> new AcessoNegadoException("Cadastro de agente não encontrado."));
    }

    private PedidoResponse responder(PedidoAluguel pedido) {
        Contrato contrato = contratoRepository.findByPedidoId(pedido.getId()).orElse(null);
        return PedidoResponse.de(pedido, contrato);
    }

    /** Monta as respostas de uma lista buscando os contratos de uma vez só. */
    private List<PedidoResponse> responder(List<PedidoAluguel> pedidos) {
        if (pedidos.isEmpty()) {
            return List.of();
        }
        List<Long> ids = pedidos.stream().map(PedidoAluguel::getId).toList();
        Map<Long, Contrato> contratosPorPedido = contratoRepository.findByPedidoIdIn(ids).stream()
                .collect(Collectors.toMap(contrato -> contrato.getPedido().getId(), Function.identity()));
        return pedidos.stream()
                .map(pedido -> PedidoResponse.de(pedido, contratosPorPedido.get(pedido.getId())))
                .toList();
    }
}

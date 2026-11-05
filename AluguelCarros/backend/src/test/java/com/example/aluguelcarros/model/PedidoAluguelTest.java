package com.example.aluguelcarros.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PedidoAluguelTest {

    private final LocalDate inicio = LocalDate.of(2026, 12, 1);
    private final LocalDate fim = LocalDate.of(2026, 12, 5);

    private Cliente cliente;
    private Automovel automovel;
    private Agente agente;
    private PedidoAluguel pedido;

    @BeforeEach
    void preparar() {
        cliente = new Cliente(new Usuario("maria", "hash", Perfil.CLIENTE), "MG-123", "52998224725", "Maria",
                "Rua A, 10", "Engenheira", List.of());
        automovel = new Automovel("MAT-1", 2023, "Fiat", "Argo", "ABC1D23", TipoProprietario.EMPRESA);
        agente = new Agente(new Usuario("agente", "hash", Perfil.AGENTE), "Banco Horizonte", TipoAgente.BANCO);
        pedido = new PedidoAluguel(cliente, automovel, inicio, fim);
    }

    @Test
    void pedidoNovoComecaEmAnalise() {
        assertEquals(StatusPedido.EM_ANALISE, pedido.getStatus());
    }

    @Test
    void aprovacaoRegistraOAgenteEOParecer() {
        pedido.avaliar(agente, Decisao.APROVAR, "Renda compatível");

        assertEquals(StatusPedido.APROVADO, pedido.getStatus());
        assertEquals(agente, pedido.getAvaliadoPor());
        assertEquals("Renda compatível", pedido.getParecer());
    }

    @Test
    void reprovacaoEncerraOPedido() {
        pedido.avaliar(agente, Decisao.REPROVAR, "Renda insuficiente");

        assertEquals(StatusPedido.REPROVADO, pedido.getStatus());
        assertThrows(IllegalStateException.class, () -> pedido.avaliar(agente, Decisao.APROVAR, null));
    }

    @Test
    void naoEPossivelContratarSemAprovacao() {
        assertThrows(IllegalStateException.class, pedido::marcarComoContratado);
    }

    @Test
    void pedidoAprovadoPodeSerContratado() {
        pedido.avaliar(agente, Decisao.APROVAR, null);
        pedido.marcarComoContratado();

        assertEquals(StatusPedido.CONTRATADO, pedido.getStatus());
    }

    @Test
    void pedidoCanceladoNaoPodeSerCanceladoDeNovo() {
        pedido.cancelar();

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertThrows(IllegalStateException.class, pedido::cancelar);
    }

    @Test
    void soEPossivelAlterarEnquantoEmAnalise() {
        pedido.alterar(automovel, inicio.plusDays(1), fim.plusDays(1));
        assertEquals(inicio.plusDays(1), pedido.getDataInicio());

        pedido.avaliar(agente, Decisao.APROVAR, null);
        assertThrows(IllegalStateException.class, () -> pedido.alterar(automovel, inicio, fim));
    }
}

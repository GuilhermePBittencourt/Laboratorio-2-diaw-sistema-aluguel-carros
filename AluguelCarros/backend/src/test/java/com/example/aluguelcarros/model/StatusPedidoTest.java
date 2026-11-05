package com.example.aluguelcarros.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class StatusPedidoTest {

    @Test
    void pedidoEmAnaliseSoPodeSerAprovadoReprovadoOuCancelado() {
        assertTrue(StatusPedido.EM_ANALISE.podeIrPara(StatusPedido.APROVADO));
        assertTrue(StatusPedido.EM_ANALISE.podeIrPara(StatusPedido.REPROVADO));
        assertTrue(StatusPedido.EM_ANALISE.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.EM_ANALISE.podeIrPara(StatusPedido.CONTRATADO));
    }

    @Test
    void pedidoAprovadoPodeSerContratadoOuCancelado() {
        assertTrue(StatusPedido.APROVADO.podeIrPara(StatusPedido.CONTRATADO));
        assertTrue(StatusPedido.APROVADO.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.APROVADO.podeIrPara(StatusPedido.REPROVADO));
    }

    @Test
    void statusFinaisNaoMudamMais() {
        for (StatusPedido final_ : EnumSet.of(StatusPedido.REPROVADO, StatusPedido.CANCELADO, StatusPedido.CONTRATADO)) {
            for (StatusPedido destino : StatusPedido.values()) {
                assertFalse(final_.podeIrPara(destino), final_ + " -> " + destino);
            }
        }
    }

    @Test
    void somenteEmAnaliseAceitaAlteracao() {
        assertTrue(StatusPedido.EM_ANALISE.aceitaAlteracao());
        assertFalse(StatusPedido.APROVADO.aceitaAlteracao());
    }

    @Test
    void statusAtivosSaoOsQueOcupamOAutomovel() {
        assertEquals(EnumSet.of(StatusPedido.EM_ANALISE, StatusPedido.APROVADO, StatusPedido.CONTRATADO),
                StatusPedido.ativos());
        assertFalse(StatusPedido.CANCELADO.estaAtivo());
    }
}

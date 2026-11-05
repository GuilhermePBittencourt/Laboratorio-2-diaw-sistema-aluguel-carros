package com.example.aluguelcarros.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de um pedido de aluguel.
 *
 * EM_ANALISE -> APROVADO -> CONTRATADO
 * EM_ANALISE -> REPROVADO
 * EM_ANALISE ou APROVADO -> CANCELADO
 */
public enum StatusPedido {
    EM_ANALISE,
    APROVADO,
    REPROVADO,
    CANCELADO,
    CONTRATADO;

    public boolean podeIrPara(StatusPedido destino) {
        return switch (this) {
            case EM_ANALISE -> destino == APROVADO || destino == REPROVADO || destino == CANCELADO;
            case APROVADO -> destino == CONTRATADO || destino == CANCELADO;
            case REPROVADO, CANCELADO, CONTRATADO -> false;
        };
    }

    /** Dados do pedido (automóvel e datas) só podem mudar enquanto ele está em análise. */
    public boolean aceitaAlteracao() {
        return this == EM_ANALISE;
    }

    /** Pedidos nesses estados ainda "seguram" o automóvel e o cliente. */
    public boolean estaAtivo() {
        return ativos().contains(this);
    }

    public static Set<StatusPedido> ativos() {
        return EnumSet.of(EM_ANALISE, APROVADO, CONTRATADO);
    }
}

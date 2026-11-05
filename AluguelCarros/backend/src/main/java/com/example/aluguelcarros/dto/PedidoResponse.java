package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Contrato;
import com.example.aluguelcarros.model.PedidoAluguel;
import com.example.aluguelcarros.model.StatusPedido;
import java.time.Instant;
import java.time.LocalDate;

public record PedidoResponse(Long id, StatusPedido status, LocalDate dataInicio, LocalDate dataFim,
                             AutomovelResponse automovel, Long clienteId, String clienteNome, String parecer,
                             String avaliadoPor, Instant criadoEm, Instant atualizadoEm, ContratoResponse contrato) {

    /** O contrato é opcional: só existe depois que um agente executa o pedido aprovado. */
    public static PedidoResponse de(PedidoAluguel pedido, Contrato contrato) {
        String avaliador = pedido.getAvaliadoPor() == null ? null : pedido.getAvaliadoPor().getNome();
        ContratoResponse contratoResponse = contrato == null ? null : ContratoResponse.de(contrato);
        return new PedidoResponse(pedido.getId(), pedido.getStatus(), pedido.getDataInicio(), pedido.getDataFim(),
                AutomovelResponse.de(pedido.getAutomovel()), pedido.getCliente().getId(),
                pedido.getCliente().getNome(), pedido.getParecer(), avaliador, pedido.getCriadoEm(),
                pedido.getAtualizadoEm(), contratoResponse);
    }
}

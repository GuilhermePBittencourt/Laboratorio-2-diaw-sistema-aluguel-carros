package com.example.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PedidoRequest(
        @NotNull(message = "Escolha um automóvel") Long automovelId,
        @NotNull(message = "Informe a data de início") LocalDate dataInicio,
        @NotNull(message = "Informe a data de término") LocalDate dataFim) {
}

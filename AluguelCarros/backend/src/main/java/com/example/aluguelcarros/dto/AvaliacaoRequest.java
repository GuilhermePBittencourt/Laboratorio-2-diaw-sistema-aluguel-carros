package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Decisao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "Informe a decisão (APROVAR ou REPROVAR)") Decisao decisao,
        @Size(max = 500, message = "Máximo de 500 caracteres") String parecer) {
}

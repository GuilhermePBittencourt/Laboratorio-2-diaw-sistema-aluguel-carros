package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.TipoProprietario;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Execução do contrato: em nome de quem o automóvel fica e, se houver, o crédito concedido por um banco. */
public record ContratoRequest(
        @NotNull(message = "Informe em nome de quem o automóvel será registrado") TipoProprietario proprietario,
        Long bancoCreditoId,
        BigDecimal valorCredito) {
}

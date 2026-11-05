package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Rendimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RendimentoDto(
        @NotBlank(message = "Informe a entidade empregadora")
        @Size(max = 120, message = "Máximo de 120 caracteres")
        String entidadeEmpregadora,

        @NotNull(message = "Informe o valor do rendimento")
        @DecimalMin(value = "0.00", message = "O rendimento não pode ser negativo")
        BigDecimal valor) {

    public static RendimentoDto de(Rendimento rendimento) {
        return new RendimentoDto(rendimento.getEntidadeEmpregadora(), rendimento.getValor());
    }

    public Rendimento paraEntidade() {
        return new Rendimento(entidadeEmpregadora.trim(), valor);
    }
}

package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.TipoProprietario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AutomovelRequest(
        @NotBlank(message = "Informe a matrícula") @Size(max = 20, message = "Máximo de 20 caracteres") String matricula,
        @NotNull(message = "Informe o ano") @Min(value = 1950, message = "Ano inválido") @Max(value = 2100, message = "Ano inválido") Integer ano,
        @NotBlank(message = "Informe a marca") @Size(max = 60, message = "Máximo de 60 caracteres") String marca,
        @NotBlank(message = "Informe o modelo") @Size(max = 60, message = "Máximo de 60 caracteres") String modelo,
        @NotBlank(message = "Informe a placa")
        @Pattern(regexp = "^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$", message = "Placa inválida (ex.: ABC1D23)")
        String placa,
        @NotNull(message = "Informe o proprietário") TipoProprietario proprietario) {
}

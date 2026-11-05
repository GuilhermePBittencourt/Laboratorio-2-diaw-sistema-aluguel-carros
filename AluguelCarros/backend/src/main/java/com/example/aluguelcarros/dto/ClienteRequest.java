package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.validation.Cpf;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Dados de identificação, profissão e rendimentos (máximo 3) do contratante. */
public record ClienteRequest(
        @NotBlank(message = "Informe o RG") @Size(max = 20, message = "Máximo de 20 caracteres") String rg,
        @NotBlank(message = "Informe o CPF") @Cpf String cpf,
        @NotBlank(message = "Informe o nome") @Size(max = 120, message = "Máximo de 120 caracteres") String nome,
        @NotBlank(message = "Informe o endereço") @Size(max = 255, message = "Máximo de 255 caracteres") String endereco,
        @NotBlank(message = "Informe a profissão") @Size(max = 80, message = "Máximo de 80 caracteres") String profissao,
        @Size(max = 3, message = "Informe no máximo 3 rendimentos") @Valid List<RendimentoDto> rendimentos) {

    public ClienteRequest {
        if (rendimentos == null) {
            rendimentos = List.of();
        }
    }
}

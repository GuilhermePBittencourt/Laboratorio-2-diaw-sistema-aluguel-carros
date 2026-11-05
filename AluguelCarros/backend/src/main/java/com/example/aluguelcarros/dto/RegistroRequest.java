package com.example.aluguelcarros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro prévio exigido pelo sistema: credenciais mais os dados do cliente. */
public record RegistroRequest(
        @NotBlank(message = "Informe o usuário")
        @Size(min = 3, max = 50, message = "O usuário deve ter entre 3 e 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Use apenas letras, números, ponto, hífen e sublinhado")
        String login,

        @NotBlank(message = "Informe a senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha,

        @NotNull(message = "Informe os dados do cliente") @Valid ClienteRequest cliente) {
}

package com.example.aluguelcarros.exception;

import java.time.Instant;
import java.util.Map;

/** Corpo padrão das respostas de erro da API. */
public record ErroResposta(Instant timestamp, int status, String erro, String mensagem, Map<String, String> campos) {

    public static ErroResposta de(int status, String erro, String mensagem) {
        return new ErroResposta(Instant.now(), status, erro, mensagem, Map.of());
    }

    public static ErroResposta de(int status, String erro, String mensagem, Map<String, String> campos) {
        return new ErroResposta(Instant.now(), status, erro, mensagem, campos);
    }
}

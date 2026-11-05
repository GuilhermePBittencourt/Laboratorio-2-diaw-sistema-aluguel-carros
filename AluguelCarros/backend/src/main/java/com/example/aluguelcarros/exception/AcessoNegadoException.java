package com.example.aluguelcarros.exception;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends NegocioException {

    public AcessoNegadoException(String mensagem) {
        super(HttpStatus.FORBIDDEN, mensagem);
    }
}

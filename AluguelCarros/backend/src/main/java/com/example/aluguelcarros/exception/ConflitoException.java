package com.example.aluguelcarros.exception;

import org.springframework.http.HttpStatus;

public class ConflitoException extends NegocioException {

    public ConflitoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}

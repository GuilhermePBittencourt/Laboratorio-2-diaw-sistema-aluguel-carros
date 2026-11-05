package com.example.aluguelcarros.exception;

import org.springframework.http.HttpStatus;

/** Base das exceções de regra de negócio: cada uma já sabe qual status HTTP representa. */
public abstract class NegocioException extends RuntimeException {

    private final HttpStatus status;

    protected NegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

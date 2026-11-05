package com.example.aluguelcarros.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends NegocioException {

    public CredenciaisInvalidasException() {
        super(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
    }
}

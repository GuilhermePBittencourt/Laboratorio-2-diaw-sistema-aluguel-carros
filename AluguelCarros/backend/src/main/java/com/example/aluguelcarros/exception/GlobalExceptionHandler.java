package com.example.aluguelcarros.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções em respostas JSON consistentes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResposta> tratarNegocio(NegocioException ex) {
        HttpStatus status = ex.getStatus();
        return ResponseEntity.status(status).body(ErroResposta.de(status.value(), status.getReasonPhrase(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ErroResposta corpo = ErroResposta.de(400, "Bad Request", "Dados inválidos. Verifique os campos informados.", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "Bad Request", "Corpo da requisição ausente ou em formato inválido."));
    }

    /** Regras do modelo violadas (ex.: mudar o status de um pedido já cancelado). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> tratarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResposta.de(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErroResposta.de(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResposta.de(409, "Conflict", "Os dados informados conflitam com um registro existente."));
    }
}

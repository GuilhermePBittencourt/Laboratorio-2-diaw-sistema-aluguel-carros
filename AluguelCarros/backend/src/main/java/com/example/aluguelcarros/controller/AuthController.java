package com.example.aluguelcarros.controller;

import com.example.aluguelcarros.dto.LoginRequest;
import com.example.aluguelcarros.dto.LoginResponse;
import com.example.aluguelcarros.dto.RegistroRequest;
import com.example.aluguelcarros.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(@Valid @RequestBody RegistroRequest dados) {
        return authService.registrar(dados);
    }
}

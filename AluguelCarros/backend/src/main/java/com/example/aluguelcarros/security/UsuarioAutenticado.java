package com.example.aluguelcarros.security;

import com.example.aluguelcarros.model.Perfil;

/** Quem está fazendo a requisição, extraído do token JWT. É o "principal" do Spring Security. */
public record UsuarioAutenticado(Long id, String login, Perfil perfil) {

    public boolean ehCliente() {
        return perfil == Perfil.CLIENTE;
    }

    public boolean ehAgente() {
        return perfil == Perfil.AGENTE;
    }
}

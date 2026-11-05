package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Perfil;

/** Resposta de login e de cadastro: o front guarda o token e usa os demais dados na tela. */
public record LoginResponse(String token, String tipoToken, Long usuarioId, String login, String nome, Perfil perfil) {

    public static LoginResponse de(String token, Long usuarioId, String login, String nome, Perfil perfil) {
        return new LoginResponse(token, "Bearer", usuarioId, login, nome, perfil);
    }
}

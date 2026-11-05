package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Agente;
import com.example.aluguelcarros.model.TipoAgente;

public record AgenteResponse(Long id, String nome, TipoAgente tipo) {

    public static AgenteResponse de(Agente agente) {
        return new AgenteResponse(agente.getId(), agente.getNome(), agente.getTipo());
    }
}

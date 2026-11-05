package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Automovel;
import com.example.aluguelcarros.model.TipoProprietario;

public record AutomovelResponse(Long id, String matricula, int ano, String marca, String modelo, String placa,
                                TipoProprietario proprietario) {

    public static AutomovelResponse de(Automovel automovel) {
        return new AutomovelResponse(automovel.getId(), automovel.getMatricula(), automovel.getAno(),
                automovel.getMarca(), automovel.getModelo(), automovel.getPlaca(), automovel.getProprietario());
    }
}

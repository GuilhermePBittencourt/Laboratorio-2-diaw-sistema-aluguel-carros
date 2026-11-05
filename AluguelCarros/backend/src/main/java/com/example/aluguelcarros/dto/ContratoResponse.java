package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Contrato;
import com.example.aluguelcarros.model.TipoProprietario;
import java.math.BigDecimal;
import java.time.Instant;

public record ContratoResponse(Long id, TipoProprietario proprietario, String bancoCredito, BigDecimal valorCredito,
                               Instant dataContrato) {

    public static ContratoResponse de(Contrato contrato) {
        String banco = contrato.getBancoCredito() == null ? null : contrato.getBancoCredito().getNome();
        return new ContratoResponse(contrato.getId(), contrato.getProprietario(), banco,
                contrato.getValorCredito(), contrato.getDataContrato());
    }
}

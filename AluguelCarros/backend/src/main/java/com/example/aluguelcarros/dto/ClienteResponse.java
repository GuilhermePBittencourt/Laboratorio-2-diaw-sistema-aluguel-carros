package com.example.aluguelcarros.dto;

import com.example.aluguelcarros.model.Cliente;
import java.util.List;

public record ClienteResponse(Long id, String login, String rg, String cpf, String nome, String endereco,
                              String profissao, List<RendimentoDto> rendimentos) {

    public static ClienteResponse de(Cliente cliente) {
        List<RendimentoDto> rendimentos = cliente.getRendimentos().stream().map(RendimentoDto::de).toList();
        return new ClienteResponse(cliente.getId(), cliente.getUsuario().getLogin(), cliente.getRg(),
                cliente.getCpf(), cliente.getNome(), cliente.getEndereco(), cliente.getProfissao(), rendimentos);
    }
}

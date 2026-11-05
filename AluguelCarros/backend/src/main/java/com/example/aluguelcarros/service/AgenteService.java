package com.example.aluguelcarros.service;

import com.example.aluguelcarros.dto.AgenteResponse;
import com.example.aluguelcarros.model.TipoAgente;
import com.example.aluguelcarros.repository.AgenteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenteService {

    private final AgenteRepository agenteRepository;

    public AgenteService(AgenteRepository agenteRepository) {
        this.agenteRepository = agenteRepository;
    }

    /** Lista os agentes; com "tipo" informado, só os daquele tipo (ex.: bancos que podem conceder crédito). */
    @Transactional(readOnly = true)
    public List<AgenteResponse> listar(TipoAgente tipo) {
        var agentes = tipo == null
                ? agenteRepository.findAllByOrderByNomeAsc()
                : agenteRepository.findByTipoOrderByNomeAsc(tipo);
        return agentes.stream().map(AgenteResponse::de).toList();
    }
}

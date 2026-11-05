package com.example.aluguelcarros.controller;

import com.example.aluguelcarros.dto.AgenteResponse;
import com.example.aluguelcarros.model.TipoAgente;
import com.example.aluguelcarros.service.AgenteService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agentes")
@PreAuthorize("hasRole('AGENTE')")
public class AgenteController {

    private final AgenteService agenteService;

    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @GetMapping
    public List<AgenteResponse> listar(@RequestParam(required = false) TipoAgente tipo) {
        return agenteService.listar(tipo);
    }
}

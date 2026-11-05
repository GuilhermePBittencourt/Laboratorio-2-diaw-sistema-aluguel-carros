package com.example.aluguelcarros.controller;

import com.example.aluguelcarros.dto.AvaliacaoRequest;
import com.example.aluguelcarros.dto.ContratoRequest;
import com.example.aluguelcarros.dto.PedidoRequest;
import com.example.aluguelcarros.dto.PedidoResponse;
import com.example.aluguelcarros.model.StatusPedido;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import com.example.aluguelcarros.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest dados,
                                @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.criar(dados, usuario);
    }

    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) StatusPedido status,
                                       @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.listar(usuario, status);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.buscar(id, usuario);
    }

    @PutMapping("/{id}")
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest dados,
                                    @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.atualizar(id, dados, usuario);
    }

    @PostMapping("/{id}/cancelamento")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.cancelar(id, usuario);
    }

    @PostMapping("/{id}/avaliacao")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse avaliar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest dados,
                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.avaliar(id, dados, usuario);
    }

    @PostMapping("/{id}/contrato")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse contratar(@PathVariable Long id, @Valid @RequestBody ContratoRequest dados,
                                    @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.contratar(id, dados, usuario);
    }
}

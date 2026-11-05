package com.example.aluguelcarros.controller;

import com.example.aluguelcarros.dto.ClienteRequest;
import com.example.aluguelcarros.dto.ClienteResponse;
import com.example.aluguelcarros.security.UsuarioAutenticado;
import com.example.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CRUD de clientes. O cadastro (create) é feito em POST /api/auth/registro. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENTE')")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse meuCadastro(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return clienteService.buscarDoUsuario(usuario);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return clienteService.buscar(id, usuario);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest dados,
                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return clienteService.atualizar(id, dados, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        clienteService.excluir(id, usuario);
    }
}

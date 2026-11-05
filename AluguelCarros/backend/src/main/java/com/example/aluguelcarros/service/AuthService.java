package com.example.aluguelcarros.service;

import com.example.aluguelcarros.dto.LoginRequest;
import com.example.aluguelcarros.dto.LoginResponse;
import com.example.aluguelcarros.dto.RegistroRequest;
import com.example.aluguelcarros.exception.CredenciaisInvalidasException;
import com.example.aluguelcarros.model.Agente;
import com.example.aluguelcarros.model.Cliente;
import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.Usuario;
import com.example.aluguelcarros.repository.AgenteRepository;
import com.example.aluguelcarros.repository.ClienteRepository;
import com.example.aluguelcarros.repository.UsuarioRepository;
import com.example.aluguelcarros.security.JwtService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login (usuário e senha) e cadastro prévio de clientes. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AgenteRepository agenteRepository;
    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                       AgenteRepository agenteRepository, ClienteService clienteService,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.agenteRepository = agenteRepository;
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        // A mesma mensagem para "usuário inexistente" e "senha errada" evita revelar quais usuários existem.
        Usuario usuario = usuarioRepository.findByLogin(normalizar(dados.login()))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return montarResposta(usuario);
    }

    @Transactional
    public LoginResponse registrar(RegistroRequest dados) {
        Cliente cliente = clienteService.cadastrar(normalizar(dados.login()), dados.senha(), dados.cliente());
        return montarResposta(cliente.getUsuario());
    }

    private LoginResponse montarResposta(Usuario usuario) {
        return LoginResponse.de(jwtService.gerar(usuario), usuario.getId(), usuario.getLogin(),
                nomeParaExibicao(usuario), usuario.getPerfil());
    }

    private String nomeParaExibicao(Usuario usuario) {
        if (usuario.getPerfil() == Perfil.CLIENTE) {
            return clienteRepository.findByUsuarioId(usuario.getId()).map(Cliente::getNome).orElse(usuario.getLogin());
        }
        return agenteRepository.findByUsuarioId(usuario.getId()).map(Agente::getNome).orElse(usuario.getLogin());
    }

    private String normalizar(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }
}

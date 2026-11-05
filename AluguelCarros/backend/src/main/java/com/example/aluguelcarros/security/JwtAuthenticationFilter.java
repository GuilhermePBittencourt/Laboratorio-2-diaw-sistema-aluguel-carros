package com.example.aluguelcarros.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lê o cabeçalho "Authorization: Bearer ..." e, se o token for válido, autentica a requisição. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            jwtService.validar(cabecalho.substring(PREFIXO.length())).ifPresent(usuario -> {
                var autoridade = new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name());
                var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, List.of(autoridade));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            });
        }

        chain.doFilter(request, response);
    }
}

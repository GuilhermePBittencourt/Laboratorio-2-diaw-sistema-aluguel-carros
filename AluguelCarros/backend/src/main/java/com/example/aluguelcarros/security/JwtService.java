package com.example.aluguelcarros.security;

import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Gera e valida os tokens JWT usados na autenticação. */
@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-minutos:120}") long expiracaoEmMinutos) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "app.jwt.secret (variável JWT_SECRET) precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validade = Duration.ofMinutes(expiracaoEmMinutos);
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("login", usuario.getLogin())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave)
                .compact();
    }

    /** Devolve o usuário do token, ou vazio se o token for inválido, adulterado ou expirado. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            Long id = Long.valueOf(claims.getSubject());
            String login = claims.get("login", String.class);
            Perfil perfil = Perfil.valueOf(claims.get("perfil", String.class));
            return Optional.of(new UsuarioAutenticado(id, login, perfil));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

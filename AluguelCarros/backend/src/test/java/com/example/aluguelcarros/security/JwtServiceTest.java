package com.example.aluguelcarros.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = new Usuario("maria", "hash", perfil);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    void geraEValidaUmToken() {
        JwtService jwt = new JwtService(SEGREDO, 60);

        String token = jwt.gerar(usuario(7L, Perfil.CLIENTE));
        Optional<UsuarioAutenticado> resultado = jwt.validar(token);

        assertTrue(resultado.isPresent());
        assertEquals(7L, resultado.get().id());
        assertEquals("maria", resultado.get().login());
        assertEquals(Perfil.CLIENTE, resultado.get().perfil());
    }

    @Test
    void rejeitaTokenAdulteradoOuMalFormado() {
        JwtService jwt = new JwtService(SEGREDO, 60);
        String token = jwt.gerar(usuario(7L, Perfil.CLIENTE));

        assertTrue(jwt.validar(token + "x").isEmpty());
        assertTrue(jwt.validar("isto-nao-e-um-jwt").isEmpty());
    }

    @Test
    void rejeitaTokenAssinadoComOutroSegredo() {
        JwtService emissor = new JwtService("outro-segredo-bem-diferente-com-32-chars", 60);
        JwtService validador = new JwtService(SEGREDO, 60);

        assertTrue(validador.validar(emissor.gerar(usuario(7L, Perfil.AGENTE))).isEmpty());
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService jwt = new JwtService(SEGREDO, -1);

        assertTrue(jwt.validar(jwt.gerar(usuario(7L, Perfil.CLIENTE))).isEmpty());
    }

    @Test
    void naoAceitaSegredoCurto() {
        assertThrows(IllegalStateException.class, () -> new JwtService("curto", 60));
    }
}

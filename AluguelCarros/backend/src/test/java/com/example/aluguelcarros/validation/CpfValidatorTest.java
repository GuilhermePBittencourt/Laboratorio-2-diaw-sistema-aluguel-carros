package com.example.aluguelcarros.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfValidatorTest {

    @Test
    void aceitaCpfValidoComEmSemPontuacao() {
        assertTrue(CpfValidator.validar("529.982.247-25"));
        assertTrue(CpfValidator.validar("52998224725"));
        assertTrue(CpfValidator.validar("111.444.777-35"));
    }

    @Test
    void rejeitaDigitoVerificadorErrado() {
        assertFalse(CpfValidator.validar("529.982.247-24"));
    }

    @Test
    void rejeitaTamanhoErradoETodosOsDigitosIguais() {
        assertFalse(CpfValidator.validar("1234"));
        assertFalse(CpfValidator.validar("111.111.111-11"));
    }

    @Test
    void somenteDigitosRemoveAPontuacao() {
        assertEquals("52998224725", CpfValidator.somenteDigitos("529.982.247-25"));
        assertEquals("", CpfValidator.somenteDigitos(null));
    }
}

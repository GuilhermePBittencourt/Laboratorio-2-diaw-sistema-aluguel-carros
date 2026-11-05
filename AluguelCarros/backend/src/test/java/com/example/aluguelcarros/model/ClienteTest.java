package com.example.aluguelcarros.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClienteTest {

    private final Usuario usuario = new Usuario("maria", "hash", Perfil.CLIENTE);

    private Rendimento rendimento(String entidade) {
        return new Rendimento(entidade, new BigDecimal("3500.00"));
    }

    @Test
    void aceitaAteTresRendimentos() {
        List<Rendimento> tres = List.of(rendimento("A"), rendimento("B"), rendimento("C"));

        Cliente cliente = new Cliente(usuario, "MG-1", "52998224725", "Maria", "Rua A", "Engenheira", tres);

        assertEquals(3, cliente.getRendimentos().size());
    }

    @Test
    void rejeitaMaisDeTresRendimentos() {
        List<Rendimento> quatro = List.of(rendimento("A"), rendimento("B"), rendimento("C"), rendimento("D"));

        assertThrows(IllegalArgumentException.class,
                () -> new Cliente(usuario, "MG-1", "52998224725", "Maria", "Rua A", "Engenheira", quatro));
    }

    @Test
    void atualizarSubstituiOsRendimentos() {
        Cliente cliente = new Cliente(usuario, "MG-1", "52998224725", "Maria", "Rua A", "Engenheira",
                List.of(rendimento("A"), rendimento("B")));

        cliente.atualizarDados("MG-1", "52998224725", "Maria Souza", "Rua B", "Gerente", List.of(rendimento("C")));

        assertEquals("Maria Souza", cliente.getNome());
        assertEquals(1, cliente.getRendimentos().size());
        assertEquals("C", cliente.getRendimentos().get(0).getEntidadeEmpregadora());
    }
}

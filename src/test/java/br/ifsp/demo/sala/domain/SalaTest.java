package br.ifsp.demo.sala.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaTest {
    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("sala nova deve nascer disponível para reservas")
    void salaNovaDeveNascerDisponivel() {
        assertTrue(new Sala("lab1", 10).isDisponivel());
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("deve rejeitar cadastro de sala com capacidade inválida")
    void naoDeveValidarSalaComCapacidadeInvalida(){
        assertThrows(IllegalArgumentException.class,
                () -> new Sala("lab1", -1));
    }
}
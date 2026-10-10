package br.ifsp.demo.sala.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SalaTest {
    @Test
    @DisplayName("sala nova deve nascer disponível para reservas")
    void salaNovaDeveNascerDisponivel() {
        assertTrue(new Sala("lab1", 10).isDisponivel());
    }

    @Test
    @DisplayName("deve rejeitar cadastro de sala com capacidade inválida")
    void naoDeveValidarSalaComCapacidadeInvalida(){
        assertThrows(IllegalArgumentException.class,
                () -> new Sala("lab1", -1));
    }
}
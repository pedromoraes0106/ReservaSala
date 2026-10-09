package br.ifsp.demo.sala.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaTest {
    @Test
    @DisplayName("sala nova deve nascer disponível para reservas")
    void salaNovaDeveNascerDisponivel() {
        assertTrue(new Sala("lab1", 10).isDisponivel());
    }
}
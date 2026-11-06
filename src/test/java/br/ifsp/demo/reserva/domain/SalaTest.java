package br.ifsp.demo.reserva.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SalaTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("sala válida mantém identificador, nome e capacidade")
    void salaValidaMantemSeusDados() {
        UUID salaId = UUID.randomUUID();

        Sala sala = new Sala(salaId, "Lab 1", 10);

        assertThat(sala.getId()).isEqualTo(salaId);
        assertThat(sala.getNome()).isEqualTo("Lab 1");
        assertThat(sala.getCapacidade()).isEqualTo(10);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("capacidade zero é inválida")
    void deveRejeitarCapacidadeZero() {
        assertThatThrownBy(() -> new Sala(UUID.randomUUID(), "Lab 1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidade da sala deve ser maior que zero");
    }
}
package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalaServiceTddTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveAtualizarInformacoesDaSalaExistente() {
        Map<UUID, Sala> salas = new HashMap<>();
        SalaRepository salaRepository = new SalaRepository() {
            @Override
            public Optional<Sala> buscarPorId(UUID id) {
                return Optional.ofNullable(salas.get(id));
            }

            @Override
            public Sala salvar(Sala sala) {
                salas.put(sala.getId(), sala);
                return sala;
            }
        };
        SalaService service = new SalaService(salaRepository);
        UUID salaId = UUID.randomUUID();
        salaRepository.salvar(new Sala(salaId, "Sala antiga", 8));

        Sala salaAtualizada = service.editarSala(salaId, "Sala renovada", 12);

        assertThat(salaAtualizada.getId()).isEqualTo(salaId);
        assertThat(salaAtualizada.getNome()).isEqualTo("Sala renovada");
        assertThat(salaAtualizada.getCapacidade()).isEqualTo(12);
        assertThat(salaRepository.buscarPorId(salaId)).contains(salaAtualizada);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarEdicaoDeSalaInexistente() {
        SalaRepository salaRepository = new SalaRepository() {
            @Override
            public Optional<Sala> buscarPorId(UUID id) {
                return Optional.empty();
            }

            @Override
            public Sala salvar(Sala sala) {
                return sala;
            }
        };
        SalaService service = new SalaService(salaRepository);

        assertThatThrownBy(() -> service.editarSala(UUID.randomUUID(), "Sala nova", 12))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessageContaining("sala não foi encontrada");
    }
}
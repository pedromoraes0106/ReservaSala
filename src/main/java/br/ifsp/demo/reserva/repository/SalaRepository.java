package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Sala;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaRepository {
    default List<Sala> listarTodas() {
        return List.of();
    }
    Optional<Sala> buscarPorId(UUID id);
    Sala salvar(Sala sala);
    void remover(UUID id);

    default boolean existePorNome(String nome) {
        return false;
    }
}

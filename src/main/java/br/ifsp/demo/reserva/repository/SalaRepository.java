package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Sala;

import java.util.Optional;
import java.util.UUID;

public interface SalaRepository {
    Optional<Sala> buscarPorId(UUID id);
    Sala salvar(Sala sala);
}

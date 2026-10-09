package br.ifsp.demo.repository;

import br.ifsp.demo.sala.domain.Sala;

import java.util.Optional;
import java.util.UUID;

public interface SalaRepository {
    Optional<Sala> findById(UUID id);
}

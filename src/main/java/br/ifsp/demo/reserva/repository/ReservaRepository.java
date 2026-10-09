package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository {
    Optional<Reserva> buscarPorId(UUID reservaId);
    List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim);
    List<Reserva> buscarFuturasConfirmadasPorSala(UUID salaId, LocalDateTime aPartirDe);
    Reserva salvar(Reserva reserva);
}

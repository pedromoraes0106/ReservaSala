package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository {
    List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim);
    Reserva salvar(Reserva reserva);
}

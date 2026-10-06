package br.ifsp.demo.repository;

import br.ifsp.demo.reserva.domain.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository {
    List<Reserva> findConfirmadasPorSalaEDia(UUID idSala, LocalDate dia);
}

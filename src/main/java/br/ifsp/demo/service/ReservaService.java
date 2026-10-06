package br.ifsp.demo.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.repository.ReservaRepository;
import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReservaService {
    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;

    public ReservaService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
    }

    public boolean verificarDisponibilidade(UUID id, LocalDate dia, LocalTime inicio, LocalTime fim) {
        Optional<Sala> sala = salaRepository.findById(id);
        if (sala.isEmpty() || inicio == null || fim == null || !inicio.isBefore(fim)) {
            return false;
        }

        LocalDateTime inicioConsulta = LocalDateTime.of(dia, inicio);
        LocalDateTime fimConsulta = LocalDateTime.of(dia, fim);

        List<Reserva> reservas = reservaRepository.findConfirmadasPorSalaEDia(id, dia);
        if (reservas == null || reservas.isEmpty()) {
            return true;
        }

        for (Reserva reserva : reservas) {
            if (reserva.getStatus() == StatusReserva.CONFIRMADA) {
                PeriodoReserva periodo = reserva.getPeriodo();
                if (periodo.temSobreposicaoCom(inicioConsulta, fimConsulta)) {
                    return false;
                }
            }
        }

        return true;
    }
}


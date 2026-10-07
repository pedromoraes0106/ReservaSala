package br.ifsp.demo.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.repository.ReservaRepository;
import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.reserva.domain.Reserva;

import java.time.LocalDate;
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

        List<Reserva> reservas = reservaRepository.findConfirmadasPorSalaEDia(id, dia);

        return reservas.stream().noneMatch(reserva ->
                inicio.isBefore(reserva.getPeriodo().getFim())
                        && reserva.getPeriodo().getInicio().isBefore(fim));
    }

    public void editarReserva(Reserva reservaEditada) {
        if(reservaEditada == null) return;

        Optional<Reserva> reserva = reservaRepository.findById(reservaEditada.getId());

        if(reserva.isEmpty()) return;

        reserva.get().setSalaId(reservaEditada.getSalaId());
        reserva.get().setSolicitante(reservaEditada.getSolicitante());
        reserva.get().setPeriodo(reservaEditada.getPeriodo());
        reserva.get().setStatus(reservaEditada.getStatus());

        reservaRepository.update(reserva.get());
        
    }
}


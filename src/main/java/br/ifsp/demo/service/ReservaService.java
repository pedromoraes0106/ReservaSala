package br.ifsp.demo.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.sala.domain.Sala;
import br.ifsp.demo.repository.ReservaRepository;
import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public boolean verificarDisponibilidade(UUID id, LocalDate dia, LocalDateTime inicio, LocalDateTime fim) {
        return verificarDisponibilidade(id, dia, inicio, fim, null);
    }

    private boolean verificarDisponibilidade(UUID id, LocalDate dia, LocalDateTime inicio,
                                              LocalDateTime fim, UUID idReservaIgnorada) {
        Optional<Sala> sala = salaRepository.findById(id);
        if (sala.isEmpty() || inicio == null || fim == null || !inicio.isBefore(fim)) {
            return false;
        }

        List<Reserva> reservas = reservaRepository.findConfirmadasPorSalaEDia(id, dia);

        return reservas.stream()
            .filter(reserva -> idReservaIgnorada == null || !idReservaIgnorada.equals(reserva.getId()))
            .noneMatch(reserva -> inicio.isBefore(reserva.getPeriodo().getFim())
                        && reserva.getPeriodo().getInicio().isBefore(fim));
    }

    public void editarReserva(Reserva reservaEditada) {
        if (reservaEditada == null || reservaEditada.getPeriodo() == null
            || reservaEditada.getPeriodo().getInicio() == null
            || reservaEditada.getPeriodo().getFim() == null) return;

        Optional<Reserva> reserva = reservaRepository.findById(reservaEditada.getId());
        if (reserva.isEmpty()) {
            throw new ReservaNaoEncontradaException(reservaEditada.getId());
        }

        if (reserva.get().getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("A reserva " + reserva.get().getId() +" está cancelada e não pode ser editada.");
        }

        LocalDateTime inicio = reservaEditada.getPeriodo().getInicio();
        LocalDateTime fim = reservaEditada.getPeriodo().getFim();
        if (!verificarDisponibilidade(reservaEditada.getSalaId(), inicio.toLocalDate(), inicio, fim,
            reservaEditada.getId())) return;

        reserva.get().setSalaId(reservaEditada.getSalaId());
        reserva.get().setSolicitante(reservaEditada.getSolicitante());
        reserva.get().setPeriodo(reservaEditada.getPeriodo());
        reserva.get().setStatus(reservaEditada.getStatus());

        reservaRepository.update(reserva.get());
    }

    public void excluirParticipante(UUID idReserva, Participante participante) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ReservaNaoEncontradaException(idReserva));

        if(reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("A reserva " + idReserva +" está cancelada e não pode ser editada.");
        }

        if (!reserva.getParticipantes().remove(participante)) {
            throw new ParticipanteNaoEncontradoException(participante);
        }

        reservaRepository.update(reserva);
    }
}


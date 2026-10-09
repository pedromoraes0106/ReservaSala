package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;

@Service
public class ReservaService {

    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;

    public ReservaService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
    }

    public SalaRepository getSalaRepository() {
        return salaRepository;
    }

    public Reserva criarReserva(UUID salaId, String solicitante, PeriodoReserva periodo) {
        if (salaId == null) {
            throw new SalaNaoEncontradaException("sala não existe: identificador informado é inválido.");
        }
        if (solicitante == null || solicitante.isBlank()) {
            throw new IllegalArgumentException("solicitante é obrigatório.");
        }
        if (periodo == null) {
            throw new IllegalArgumentException("período inválido: informações do período são obrigatórias.");
        }

        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new SalaNaoEncontradaException("sala não existe: identificador informado não corresponde a nenhuma sala cadastrada."));

        boolean conflito = reservaRepository.buscarPorSalaEPeriodo(sala.getId(), periodo.getInicio(), periodo.getFim()).stream()
                .anyMatch(reserva -> reserva.getPeriodo().temSobreposicaoCom(periodo.getInicio(), periodo.getFim()));

        if (conflito) {
            throw new ConflitoDeHorarioException("conflito de horário: a sala já está reservada no período solicitado.");
        }

        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(), solicitante, periodo, StatusReserva.CONFIRMADA);
        return reservaRepository.salvar(reserva);
    }

    public Reserva adicionarParticipante(UUID reservaId, String nomeParticipante) {
        if (reservaId == null) {
            throw new IllegalArgumentException("identificador da reserva é obrigatório.");
        }
        if (nomeParticipante == null || nomeParticipante.isBlank()) {
            throw new IllegalArgumentException("nome do participante é obrigatório.");
        }

        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("reserva não existe."));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva não está mais ativa.");
        }

        reserva.adicionarParticipante(new Participante(nomeParticipante));
        return reservaRepository.salvar(reserva);
    }

    public Reserva cancelarReserva(UUID reservaId) {
        if (reservaId == null) {
            throw new IllegalArgumentException("identificador da reserva é obrigatório.");
        }

        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("reserva não foi encontrada."));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new IllegalArgumentException("reserva já está cancelada.");
        }

        Reserva cancelada = reserva.cancelar();
        return reservaRepository.salvar(cancelada);
    }

    public List<Reserva> listarPorSolicitante(String solicitante) {
        return reservaRepository.buscarPorSolicitante(solicitante);
    }
}

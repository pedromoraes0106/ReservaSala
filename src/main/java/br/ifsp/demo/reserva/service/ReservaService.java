package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.PeriodoInvalidoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva já está cancelada.");
        }

        Reserva cancelada = reserva.cancelar();
        return reservaRepository.salvar(cancelada);
    }

    public boolean verificarDisponibilidade(UUID salaId, LocalDate dia, LocalDateTime inicio, LocalDateTime fim) {
        if (salaId == null) {
            throw new SalaNaoEncontradaException("sala não existe: identificador informado é inválido.");
        }
        if (dia == null || inicio == null || fim == null || !inicio.isBefore(fim)
                || !inicio.toLocalDate().equals(dia) || !fim.toLocalDate().equals(dia)) {
            return false;
        }
        salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new SalaNaoEncontradaException(
                        "sala não existe: identificador informado não corresponde a nenhuma sala cadastrada."));

        return reservaRepository.buscarPorSalaEPeriodo(salaId, inicio, fim).isEmpty();
    }

    public void editarReserva(Reserva reservaEditada) {
        if (reservaEditada == null || reservaEditada.getPeriodo() == null) {
            return;
        }

        Reserva reserva = reservaRepository.buscarPorId(reservaEditada.getId())
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaEditada.getId()));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva não está mais ativa.");
        }

        LocalDateTime inicio = reservaEditada.getPeriodo().getInicio();
        LocalDateTime fim = reservaEditada.getPeriodo().getFim();
        UUID salaId = reservaEditada.getSalaId();
        if (salaId == null || salaRepository.buscarPorId(salaId).isEmpty()) {
            throw new SalaNaoEncontradaException("sala não existe: identificador informado não corresponde a nenhuma sala cadastrada.");
        }

        boolean conflito = reservaRepository
                .buscarPorSalaEPeriodo(salaId, inicio, fim)
                .stream()
                .anyMatch(outra -> !outra.getId().equals(reserva.getId()));
        if (conflito) {
            throw new ConflitoDeHorarioException("conflito de horário: a sala já está reservada no período solicitado.");
        }

        reserva.setSalaId(reservaEditada.getSalaId());
        reserva.setSolicitante(reservaEditada.getSolicitante());
        reserva.setPeriodo(reservaEditada.getPeriodo());
        reservaRepository.salvar(reserva);
    }

    public void excluirParticipante(UUID reservaId, Participante participante) {
        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva não está mais ativa.");
        }

        if (!reserva.getParticipantes().remove(participante)) {
            throw new ParticipanteNaoEncontradoException(participante);
        }

        reservaRepository.salvar(reserva);
    }

    public void excluirParticipante(UUID reservaId, String nomeParticipante) {
        if (nomeParticipante == null || nomeParticipante.isBlank()) {
            throw new IllegalArgumentException("nome do participante é obrigatório.");
        }

        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva não está mais ativa.");
        }

        Participante participante = reserva.getParticipantes().stream()
                .filter(item -> item.getNome().equals(nomeParticipante.trim()))
                .findFirst()
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(new Participante(nomeParticipante)));
        reserva.getParticipantes().remove(participante);
        reservaRepository.salvar(reserva);
    }

    public List<Reserva> listarPorSolicitante(String solicitante) {
        return reservaRepository.buscarPorSolicitante(solicitante);
    }

    public Reserva confirmarCheckIn(UUID reservaId, LocalDateTime horarioAtual) {
        if (reservaId == null) {
            throw new IllegalArgumentException("identificador da reserva é obrigatório.");
        }
        if (horarioAtual == null) {
            throw new IllegalArgumentException("horário do check-in é obrigatório.");
        }

        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("reserva não foi encontrada."));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaCanceladaException("reserva não está mais ativa.");
        }

        if (horarioAtual.isBefore(reserva.getPeriodo().getInicio()) || horarioAtual.isAfter(reserva.getPeriodo().getFim())) {
            throw new IllegalArgumentException("check-in só é permitido dentro do período reservado.");
        }

        Reserva reservaEmUso = new Reserva(
                reserva.getId(),
                reserva.getSalaId(),
                reserva.getSolicitante(),
                reserva.getPeriodo(),
                StatusReserva.EM_USO,
                reserva.getParticipantes()
        );

        return reservaRepository.salvar(reservaEmUso);
    }

    public List<Reserva> consultarReservas(UUID salaId, LocalDateTime inicio, LocalDateTime fim, String solicitante) {
        if ((inicio == null) != (fim == null)) {
            throw new IllegalArgumentException("período inválido: início e fim devem ser informados juntos.");
        }

        if (inicio != null && fim != null && !fim.isAfter(inicio)) {
            throw new PeriodoInvalidoException("período inválido: a data final deve ser posterior à data inicial.");
        }

        return reservaRepository.buscarComFiltros(salaId, inicio, fim, solicitante);
    }
}

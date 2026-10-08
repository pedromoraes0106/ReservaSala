package br.ifsp.demo.reserva.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Reserva {
    private final UUID id;
    private final UUID salaId;
    private final String solicitante;
    private final PeriodoReserva periodo;
    private final StatusReserva status;
    private final List<Participante> participantes;

    public Reserva(UUID id, UUID salaId, String solicitante, PeriodoReserva periodo, StatusReserva status) {
        this(id, salaId, solicitante, periodo, status, new ArrayList<>());
    }

    public Reserva(UUID id, UUID salaId, String solicitante, PeriodoReserva periodo, StatusReserva status, List<Participante> participantes) {
        this.id = id;
        this.salaId = salaId;
        this.solicitante = solicitante;
        this.periodo = periodo;
        this.status = status;
        this.participantes = new ArrayList<>(participantes == null ? List.of() : participantes);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public PeriodoReserva getPeriodo() {
        return periodo;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public void adicionarParticipante(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("participante é obrigatório.");
        }
        if (participantes.contains(participante)) {
            throw new IllegalArgumentException("participante já está na reserva.");
        }
        participantes.add(participante);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva reserva)) return false;
        return Objects.equals(id, reserva.id) && Objects.equals(salaId, reserva.salaId) && Objects.equals(solicitante, reserva.solicitante) && Objects.equals(periodo, reserva.periodo) && status == reserva.status && Objects.equals(participantes, reserva.participantes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, salaId, solicitante, periodo, status, participantes);
    }
}

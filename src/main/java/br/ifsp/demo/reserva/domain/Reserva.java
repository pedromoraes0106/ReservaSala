package br.ifsp.demo.reserva.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Reserva {
    private final UUID id;
    private UUID salaId;
    private String solicitante;
    private PeriodoReserva periodo;
    private StatusReserva status;
    private List<Participante> participantes;

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

    public void setPeriodo(PeriodoReserva periodo) {

        this.periodo = periodo;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
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

    public Reserva cancelar() {
        return new Reserva(this.id, this.salaId, this.solicitante, this.periodo, StatusReserva.CANCELADA, this.participantes);
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public void adicionarParticipante(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("participante é obrigatório.");
        }
        boolean participanteJaAdicionado = participantes.stream()
            .anyMatch(existente -> existente.getNome().equals(participante.getNome()));
        if (participanteJaAdicionado) {
            throw new IllegalArgumentException("participante já está na reserva.");
        }
        participantes.add(participante);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id) && Objects.equals(salaId, reserva.salaId) && Objects.equals(solicitante, reserva.solicitante) && Objects.equals(periodo, reserva.periodo) && status == reserva.status && Objects.equals(participantes, reserva.participantes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, salaId, solicitante, periodo, status, participantes);
    }
}

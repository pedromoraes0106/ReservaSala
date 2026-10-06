package br.ifsp.demo.reserva.domain;

import java.util.Objects;
import java.util.UUID;

public class Reserva {
    private final UUID id;
    private final UUID salaId;
    private final String solicitante;
    private final PeriodoReserva periodo;
    private final StatusReserva status;

    public Reserva(UUID id, UUID salaId, String solicitante, PeriodoReserva periodo, StatusReserva status) {
        this.id = id;
        this.salaId = salaId;
        this.solicitante = solicitante;
        this.periodo = periodo;
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva reserva)) return false;
        return Objects.equals(id, reserva.id) && Objects.equals(salaId, reserva.salaId) && Objects.equals(solicitante, reserva.solicitante) && Objects.equals(periodo, reserva.periodo) && status == reserva.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, salaId, solicitante, periodo, status);
    }
}
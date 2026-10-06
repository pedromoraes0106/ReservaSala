package br.ifsp.demo.reserva.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public class CriarReservaRequest {
    private UUID salaId;
    private String solicitante;
    private LocalDateTime inicio;
    private LocalDateTime fim;

    public CriarReservaRequest() {
    }

    public CriarReservaRequest(UUID salaId, String solicitante, LocalDateTime inicio, LocalDateTime fim) {
        this.salaId = salaId;
        this.solicitante = solicitante;
        this.inicio = inicio;
        this.fim = fim;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }
}

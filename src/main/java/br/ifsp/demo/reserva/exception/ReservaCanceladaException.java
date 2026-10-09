package br.ifsp.demo.reserva.exception;

import java.util.UUID;

public class ReservaCanceladaException extends RuntimeException {
    public ReservaCanceladaException(UUID id) {
        super("Reserva " + id + " está cancelada e não pode ser editada.");
    }
}

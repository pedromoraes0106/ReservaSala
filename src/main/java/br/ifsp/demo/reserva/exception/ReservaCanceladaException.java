package br.ifsp.demo.reserva.exception;

import java.util.UUID;

public class ReservaCanceladaException extends RuntimeException {
    public ReservaCanceladaException(String message) {
        super(message);
    }
}

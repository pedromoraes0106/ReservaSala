package br.ifsp.demo.reserva.exception;

import java.util.UUID;

public class ReservaNaoEncontradaException extends RuntimeException {
    public ReservaNaoEncontradaException(UUID id) {
        super("Reserva " + id + " inexistente e não pode ser editada.");
    }
}

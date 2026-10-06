package br.ifsp.demo.reserva.exception;

public class PeriodoInvalidoException extends IllegalArgumentException {
    public PeriodoInvalidoException(String message) {
        super(message);
    }
}

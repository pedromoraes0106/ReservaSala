package br.ifsp.demo.reserva.exception;

public class ConflitoDeHorarioException extends IllegalStateException {
    public ConflitoDeHorarioException(String message) {
        super(message);
    }
}

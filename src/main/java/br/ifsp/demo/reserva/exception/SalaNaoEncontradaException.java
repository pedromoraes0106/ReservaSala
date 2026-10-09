package br.ifsp.demo.reserva.exception;

public class SalaNaoEncontradaException extends IllegalArgumentException {
    public SalaNaoEncontradaException(String message) {
        super(message);
    }
}

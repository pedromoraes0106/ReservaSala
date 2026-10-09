package br.ifsp.demo.sala.exception;

public class NomeEmUsoException extends RuntimeException {
    public NomeEmUsoException(String nome) {
        super("Nome já está em uso: " + nome);
    }
}

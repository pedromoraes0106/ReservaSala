package br.ifsp.demo.reserva.domain;

import java.util.Objects;

public class Participante {
    private final String nome;

    public Participante(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do participante é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participante that)) return false;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}

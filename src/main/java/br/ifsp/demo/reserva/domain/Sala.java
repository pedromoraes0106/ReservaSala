package br.ifsp.demo.reserva.domain;

import java.util.Objects;
import java.util.UUID;

public class Sala {
    private final UUID id;
    private final String nome;
    private final int capacidade;

    public Sala(UUID id, String nome, int capacidade) {
        if (id == null) {
            throw new IllegalArgumentException("identificador da sala é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome da sala é obrigatório.");
        }
        if (capacidade <= 0) {
            throw new IllegalArgumentException("capacidade da sala deve ser maior que zero.");
        }
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sala sala)) return false;
        return capacidade == sala.capacidade && Objects.equals(id, sala.id) && Objects.equals(nome, sala.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, capacidade);
    }
}

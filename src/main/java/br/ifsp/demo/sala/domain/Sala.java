package br.ifsp.demo.sala.domain;

import java.util.UUID;

public class Sala {
    private final UUID id;
    private final String nome;
    private final int capacidade;
    private final boolean disponivel;

    public Sala(String nome, int capacidade) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.capacidade = capacidade;
        this.disponivel = true;
    }

    public UUID getId() { return id; }
    public boolean isDisponivel() { return disponivel; }

}

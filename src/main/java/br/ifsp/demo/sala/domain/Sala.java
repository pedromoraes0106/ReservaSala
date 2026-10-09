package br.ifsp.demo.sala.domain;

import java.util.UUID;

public class Sala {
    private UUID id;
    private String nome;
    private int capacidade;

    public Sala(String nome, int capacidade) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.capacidade = capacidade;
    }

    public UUID getId() {
        return id;
    }

}

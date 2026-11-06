package br.ifsp.demo.reserva.domain;

import java.util.Objects;
import java.util.UUID;

public class Participante {
    private UUID id;    
    private final String nome;

    public Participante(UUID id, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do participante é obrigatório.");
        }
        this.id = id;
        this.nome = nome.trim();
    }

    public Participante(String nome) {
        this(UUID.randomUUID(), nome);
    }


    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

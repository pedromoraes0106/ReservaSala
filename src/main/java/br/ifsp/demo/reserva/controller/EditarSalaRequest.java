package br.ifsp.demo.reserva.controller;

public class EditarSalaRequest {
    private String nome;
    private int capacidade;

    public EditarSalaRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }
}
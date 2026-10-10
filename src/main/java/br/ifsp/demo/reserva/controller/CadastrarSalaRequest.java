package br.ifsp.demo.reserva.controller;

public class CadastrarSalaRequest {
    private String nome;
    private int capacidade;

    public CadastrarSalaRequest() {
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
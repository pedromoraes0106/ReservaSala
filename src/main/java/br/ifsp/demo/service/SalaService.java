package br.ifsp.demo.service;

import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.sala.domain.Sala;

public class SalaService {
    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void cadastrarSala(Sala sala) {
        salaRepository.save(sala);
    }
}

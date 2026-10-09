package br.ifsp.demo.service;

import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.sala.domain.Sala;
import br.ifsp.demo.sala.exception.NomeEmUsoException;

public class SalaService {
    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void cadastrarSala(Sala sala) {
        if(salaRepository.existsByNome(sala.getNome())){
            throw new NomeEmUsoException(sala.getNome());
        }
        salaRepository.save(sala);
    }
}

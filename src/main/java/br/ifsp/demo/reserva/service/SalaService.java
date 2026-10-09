package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public Sala editarSala(UUID salaId, String novoNome, int novaCapacidade) {
        Sala salaExistente = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new SalaNaoEncontradaException("sala não foi encontrada."));

        Sala salaAtualizada = new Sala(salaExistente.getId(), novoNome, novaCapacidade);
        return salaRepository.salvar(salaAtualizada);
    }
}
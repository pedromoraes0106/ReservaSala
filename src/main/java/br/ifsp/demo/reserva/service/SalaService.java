package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class SalaService {

    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;

    public SalaService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
    }

    public Sala cadastrarSala(String nome, int capacidade) {
        Sala sala = new Sala(UUID.randomUUID(), nome, capacidade);
        return salaRepository.salvar(sala);
    }

    public Sala editarSala(UUID salaId, String novoNome, int novaCapacidade) {
        Sala salaExistente = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new SalaNaoEncontradaException("sala não foi encontrada."));

        boolean conflitoCapacidade = reservaRepository
            .buscarFuturasConfirmadasPorSala(salaId, LocalDateTime.now())
            .stream()
            .anyMatch(reserva -> reserva.getParticipantes().size() > novaCapacidade);

        if (conflitoCapacidade) {
            throw new IllegalArgumentException("conflito com reservas existentes: capacidade menor que o número de participantes.");
        }

        Sala salaAtualizada = new Sala(salaExistente.getId(), novoNome, novaCapacidade);
        return salaRepository.salvar(salaAtualizada);
    }

    public void removerSala(UUID salaId) {
        Sala salaExistente = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new SalaNaoEncontradaException("sala não foi encontrada."));

        boolean possuiReservasPendentes = !reservaRepository
                .buscarFuturasConfirmadasPorSala(salaId, LocalDateTime.now())
                .isEmpty();

        if (possuiReservasPendentes) {
            throw new IllegalArgumentException("sala possui reservas pendentes e não pode ser removida.");
        }

        salaRepository.remover(salaExistente.getId());
    }
}
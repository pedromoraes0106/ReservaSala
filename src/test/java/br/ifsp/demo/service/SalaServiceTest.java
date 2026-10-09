package br.ifsp.demo.service;

import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.sala.domain.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SalaServiceTest {
    private SalaRepository salaRepository;
    private SalaService salaService;

    @BeforeEach
    void setUp() {
        salaRepository = mock(SalaRepository.class);
        salaService = new SalaService(salaRepository);
    }

    @Test
    @DisplayName("deve cadastrar uma sala com sucesso")
    void validarCadastroSala(){
        Sala sala = new Sala("lab1",10);

        salaService.cadastrarSala(sala);

        verify(salaRepository).save(sala);
    }
}
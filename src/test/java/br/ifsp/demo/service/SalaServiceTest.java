package br.ifsp.demo.service;

import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.sala.domain.Sala;
import br.ifsp.demo.sala.exception.NomeEmUsoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SalaServiceTest {
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

    @Test
    @DisplayName("deve rejeitar cadastro de sala com mesmo nome")
    void naoDeveValidarSalaComMesmoNome(){
        when(salaRepository.existsByNome("lab1")).thenReturn(true);
        Sala sala = new Sala("lab1", 10);

        assertThrows(NomeEmUsoException.class,
                () -> salaService.cadastrarSala(sala));

        verify(salaRepository, never()).save(any());
    }
}
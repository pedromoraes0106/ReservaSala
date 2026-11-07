package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("Mutation")
@ExtendWith(MockitoExtension.class)
public class ReservaServiceMutationTest {

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private ReservaService service;

    @BeforeEach
    void setUp() {
        service = new ReservaService(salaRepository, reservaRepository);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void naoDeveLancarConflitoQuandoUnicaReservaEncontradaEAPropria() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 10, 11, 0);
        PeriodoReserva periodo = new PeriodoReserva(inicio, fim);

        Reserva reservaExistente = new Reserva(reservaId, salaId, "Maria", periodo, StatusReserva.CONFIRMADA);
        Reserva reservaEditada = new Reserva(reservaId, salaId, "Maria", periodo, StatusReserva.CONFIRMADA);

        when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reservaExistente));
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(mock(Sala.class)));
        when(reservaRepository.buscarPorSalaEPeriodo(salaId, inicio, fim))
                .thenReturn(List.of(reservaExistente));

        assertDoesNotThrow(() -> service.editarReserva(reservaEditada));

        verify(reservaRepository).salvar(reservaExistente);
    }
}

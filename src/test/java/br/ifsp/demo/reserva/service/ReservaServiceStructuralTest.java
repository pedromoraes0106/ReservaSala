package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReservaServiceStructuralTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRejeitarCriacaoComIdDeSalaNulo() {
        ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

        assertThatThrownBy(() -> service.criarReserva(null, "Pedro", periodoValido()))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessageContaining("identificador informado é inválido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRejeitarCriacaoComSolicitanteNulo() {
        ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

        assertThatThrownBy(() -> service.criarReserva(UUID.randomUUID(), null, periodoValido()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solicitante é obrigatório");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRejeitarCriacaoComSolicitanteEmBranco() {
        ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

        assertThatThrownBy(() -> service.criarReserva(UUID.randomUUID(), "   ", periodoValido()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solicitante é obrigatório");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRejeitarCriacaoComPeriodoNulo() {
        ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

        assertThatThrownBy(() -> service.criarReserva(UUID.randomUUID(), "Pedro", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período inválido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRejeitarCriacaoQuandoExisteConflitoDeHorario() {
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        PeriodoReserva periodoSolicitado = new PeriodoReserva(inicio, inicio.plusHours(1));
        Sala sala = new Sala(salaId, "Sala ocupada", 8);
        Reserva reservaExistente = new Reserva(
                UUID.randomUUID(), salaId, "Maria",
                new PeriodoReserva(inicio.plusMinutes(30), inicio.plusHours(2)),
                StatusReserva.CONFIRMADA
        );
        SalaRepository salaRepository = mock(SalaRepository.class);
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(sala));
        when(reservaRepository.buscarPorSalaEPeriodo(salaId, periodoSolicitado.getInicio(), periodoSolicitado.getFim()))
                .thenReturn(List.of(reservaExistente));
        ReservaService service = criarService(salaRepository, reservaRepository);

        assertThatThrownBy(() -> service.criarReserva(salaId, "Pedro", periodoSolicitado))
                .isInstanceOf(ConflitoDeHorarioException.class)
                .hasMessageContaining("conflito de horário");
    }

    private ReservaService criarService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        return new ReservaService(salaRepository, reservaRepository);
    }

    private PeriodoReserva periodoValido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        return new PeriodoReserva(inicio, inicio.plusHours(1));
    }

}

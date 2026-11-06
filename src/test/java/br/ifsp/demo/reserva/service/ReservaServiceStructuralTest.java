package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirValidacoesDeAdicionarParticipante() {
            ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

            assertThatThrownBy(() -> service.adicionarParticipante(null, "Maria"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador da reserva é obrigatório");
            assertThatThrownBy(() -> service.adicionarParticipante(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome do participante é obrigatório");
            assertThatThrownBy(() -> service.adicionarParticipante(UUID.randomUUID(), "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome do participante é obrigatório");
            assertThatThrownBy(() -> service.adicionarParticipante(UUID.randomUUID(), "Maria"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserva não existe");
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirParametrosInvalidosDeDisponibilidade() {
            ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));
            UUID salaId = UUID.randomUUID();
            LocalDateTime inicio = periodoValido().getInicio();
            LocalDate dia = inicio.toLocalDate();
            LocalDateTime fim = inicio.plusHours(1);

            assertThatThrownBy(() -> service.verificarDisponibilidade(null, dia, inicio, fim))
                .isInstanceOf(SalaNaoEncontradaException.class);
            assertThat(service.verificarDisponibilidade(salaId, null, inicio, fim)).isFalse();
            assertThat(service.verificarDisponibilidade(salaId, dia, null, fim)).isFalse();
            assertThat(service.verificarDisponibilidade(salaId, dia, inicio, null)).isFalse();
            assertThat(service.verificarDisponibilidade(salaId, dia, fim, inicio)).isFalse();
            assertThat(service.verificarDisponibilidade(salaId, dia.minusDays(1), inicio, fim)).isFalse();
            assertThat(service.verificarDisponibilidade(salaId, dia, inicio, fim.plusDays(1))).isFalse();
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirDadosAusentesNaEdicaoDeReserva() {
            ReservaService serviceSemRepositorios = criarService(
                mock(SalaRepository.class), mock(ReservaRepository.class));
            serviceSemRepositorios.editarReserva(null);
            serviceSemRepositorios.editarReserva(
                new Reserva(UUID.randomUUID(), UUID.randomUUID(), "Pedro", null, StatusReserva.CONFIRMADA));

            UUID salaId = UUID.randomUUID();
            Reserva reservaExistente = new Reserva(
                UUID.randomUUID(), salaId, "Pedro", periodoValido(), StatusReserva.CONFIRMADA);
            ReservaRepository reservaRepository = mock(ReservaRepository.class);
            SalaRepository salaRepository = mock(SalaRepository.class);
            when(reservaRepository.buscarPorId(reservaExistente.getId())).thenReturn(Optional.of(reservaExistente));
            when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.empty());
            ReservaService serviceComSalaInexistente = criarService(salaRepository, reservaRepository);

            assertThatThrownBy(() -> serviceComSalaInexistente.editarReserva(reservaExistente))
                .isInstanceOf(SalaNaoEncontradaException.class);

            Reserva alteracoesSemSala = new Reserva(
                reservaExistente.getId(), null, "Pedro", periodoValido(), StatusReserva.CONFIRMADA);
            assertThatThrownBy(() -> serviceComSalaInexistente.editarReserva(alteracoesSemSala))
                .isInstanceOf(SalaNaoEncontradaException.class);
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirValidacoesDeRemocaoDeParticipantePorNome() {
            UUID reservaId = UUID.randomUUID();
            ReservaRepository reservaRepository = mock(ReservaRepository.class);
            ReservaService service = criarService(mock(SalaRepository.class), reservaRepository);

            assertThatThrownBy(() -> service.excluirParticipante(reservaId, (String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome do participante é obrigatório");
            assertThatThrownBy(() -> service.excluirParticipante(reservaId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome do participante é obrigatório");
            assertThatThrownBy(() -> service.excluirParticipante(UUID.randomUUID(), "Maria"))
                .isInstanceOf(br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException.class);
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveRejeitarRemocaoPorNomeDeReservaCancelada() {
            UUID reservaId = UUID.randomUUID();
            Reserva reservaCancelada = new Reserva(
                reservaId,
                UUID.randomUUID(),
                "Pedro",
                periodoValido(),
                StatusReserva.CANCELADA
            );
            ReservaRepository reservaRepository = mock(ReservaRepository.class);
            when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reservaCancelada));
            ReservaService service = criarService(mock(SalaRepository.class), reservaRepository);

            assertThatThrownBy(() -> service.excluirParticipante(reservaId, "Maria"))
                .isInstanceOf(br.ifsp.demo.reserva.exception.ReservaCanceladaException.class)
                .hasMessageContaining("reserva não está mais ativa");
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveRejeitarRemocaoDeParticipanteComReservaInexistente() {
            ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));

            assertThatThrownBy(() -> service.excluirParticipante(
                UUID.randomUUID(), new Participante("Maria")))
                .isInstanceOf(br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException.class);
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirValidacoesDeCheckIn() {
            UUID reservaId = UUID.randomUUID();
            ReservaService serviceSemReserva = criarService(
                mock(SalaRepository.class), mock(ReservaRepository.class));

            assertThatThrownBy(() -> serviceSemReserva.confirmarCheckIn(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador da reserva é obrigatório");
            assertThatThrownBy(() -> serviceSemReserva.confirmarCheckIn(reservaId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horário do check-in é obrigatório");
            assertThatThrownBy(() -> serviceSemReserva.confirmarCheckIn(reservaId, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserva não foi encontrada");

            LocalDateTime inicio = periodoValido().getInicio();
            Reserva reserva = new Reserva(
                reservaId, UUID.randomUUID(), "Pedro", periodoValido(), StatusReserva.CONFIRMADA);
            ReservaRepository reservaRepository = mock(ReservaRepository.class);
            when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reserva));
            ReservaService serviceComReserva = criarService(mock(SalaRepository.class), reservaRepository);

            assertThatThrownBy(() -> serviceComReserva.confirmarCheckIn(reservaId, inicio.minusNanos(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dentro do período reservado");
            }

            @Test
            @Tag("UnitTest")
            @Tag("Structural")
            void deveCobrirFiltrosParciaisEPeriodoInvalidoNaConsulta() {
            ReservaService service = criarService(mock(SalaRepository.class), mock(ReservaRepository.class));
            LocalDateTime inicio = periodoValido().getInicio();

            assertThatThrownBy(() -> service.consultarReservas(null, inicio, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("início e fim devem ser informados juntos");
            assertThatThrownBy(() -> service.consultarReservas(null, null, inicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("início e fim devem ser informados juntos");
            assertThatThrownBy(() -> service.consultarReservas(null, inicio, inicio, null))
                .isInstanceOf(br.ifsp.demo.reserva.exception.PeriodoInvalidoException.class);
            }

    private ReservaService criarService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        return new ReservaService(salaRepository, reservaRepository);
    }

    private PeriodoReserva periodoValido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        return new PeriodoReserva(inicio, inicio.plusHours(1));
    }

}

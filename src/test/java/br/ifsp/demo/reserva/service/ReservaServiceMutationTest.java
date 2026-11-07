package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveAtualizarSalaDaReservaAoEditar() {
        UUID reservaId = UUID.randomUUID();
        UUID salaOriginalId = UUID.randomUUID();
        UUID salaNovaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 10, 11, 0);
        PeriodoReserva periodo = new PeriodoReserva(inicio, fim);

        Reserva reservaExistente = new Reserva(reservaId, salaOriginalId, "Maria", periodo, StatusReserva.CONFIRMADA);
        Reserva reservaEditada = new Reserva(reservaId, salaNovaId, "Maria", periodo, StatusReserva.CONFIRMADA);

        when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reservaExistente));
        when(salaRepository.buscarPorId(salaNovaId)).thenReturn(Optional.of(mock(Sala.class)));
        when(reservaRepository.buscarPorSalaEPeriodo(salaNovaId, inicio, fim)).thenReturn(List.of());

        service.editarReserva(reservaEditada);

        assertThat(reservaExistente.getSalaId()).isEqualTo(salaNovaId);
        verify(reservaRepository).salvar(reservaExistente);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void naoDeveLancarConflitoQuandoReservaExistenteNaoSobrepoeOPeriodo() {
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 10, 11, 0);
        PeriodoReserva periodoSolicitado = new PeriodoReserva(inicio, fim);

        PeriodoReserva periodoExistente = new PeriodoReserva(
                LocalDateTime.of(2026, 11, 10, 14, 0),
                LocalDateTime.of(2026, 11, 10, 15, 0));
        Reserva reservaExistente = new Reserva(UUID.randomUUID(), salaId, "Joao", periodoExistente, StatusReserva.CONFIRMADA);

        Sala sala = mock(Sala.class);
        when(sala.getId()).thenReturn(salaId);
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(sala));
        when(reservaRepository.buscarPorSalaEPeriodo(salaId, inicio, fim)).thenReturn(List.of(reservaExistente));
        when(reservaRepository.salvar(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = service.criarReserva(salaId, "Maria", periodoSolicitado);

        assertThat(resultado.getSalaId()).isEqualTo(salaId);
        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        verify(reservaRepository).salvar(any(Reserva.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveExcluirParticipanteAnaMantendoBruno() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0));

        Reserva reserva = new Reserva(reservaId, salaId, "Maria", periodo, StatusReserva.CONFIRMADA);
        reserva.adicionarParticipante(new Participante("Ana"));
        reserva.adicionarParticipante(new Participante("Bruno"));

        when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reserva));

        service.excluirParticipante(reservaId, "Ana");

        assertThat(reserva.getParticipantes())
                .extracting(Participante::getNome)
                .containsExactly("Bruno");
        verify(reservaRepository).salvar(reserva);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveExcluirParticipanteBrunoMantendoAna() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0));

        Reserva reserva = new Reserva(reservaId, salaId, "Maria", periodo, StatusReserva.CONFIRMADA);
        reserva.adicionarParticipante(new Participante("Ana"));
        reserva.adicionarParticipante(new Participante("Bruno"));

        when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reserva));

        service.excluirParticipante(reservaId, "Bruno");

        assertThat(reserva.getParticipantes())
                .extracting(Participante::getNome)
                .containsExactly("Ana");
        verify(reservaRepository).salvar(reserva);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveRetornarAReservaSalvaAoAdicionarParticipante() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0));

        Reserva reserva = new Reserva(reservaId, salaId, "Maria", periodo, StatusReserva.CONFIRMADA);

        when(reservaRepository.buscarPorId(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepository.salvar(reserva)).thenReturn(reserva);

        Reserva resultado = service.adicionarParticipante(reservaId, "Ana");

        assertThat(resultado).isSameAs(reserva);
        assertThat(resultado.getParticipantes())
                .extracting(Participante::getNome)
                .containsExactly("Ana");
    }
}

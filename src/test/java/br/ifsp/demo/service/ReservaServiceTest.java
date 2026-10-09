package br.ifsp.demo.service;

import br.ifsp.demo.reserva.domain.*;
import br.ifsp.demo.repository.ReservaRepository;
import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import br.ifsp.demo.sala.domain.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

public class ReservaServiceTest {
    private ReservaRepository reservaRepository;
    private SalaRepository salaRepository;
    private ReservaService service;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        salaRepository = mock(SalaRepository.class);
        service = new ReservaService(salaRepository, reservaRepository);
    }


    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Sala disponível quando não há reservas no período informado")
    void validaVerificacaodeDisponibilidade() {

        Sala sala = new Sala("Lab 1", 50);
        LocalDate dia = LocalDate.of(2020, 1, 1);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));
        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(), dia))
                .thenReturn(List.of());

        boolean disponivel = service.verificarDisponibilidade(sala.getId(), dia,
                LocalDateTime.of(dia, LocalTime.of(10, 0)), LocalDateTime.of(dia, LocalTime.of(12, 0)));

        assertThat(disponivel).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar quando o período na sala já está alugado")
    void validarConflitoDeHorario() {

        Sala sala = new Sala("Lab 1", 50);
        LocalDate dia = LocalDate.of(2020, 1, 1);

        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(), "solicitante",
                new PeriodoReserva(LocalDateTime.of(dia, LocalTime.of(10, 0)),
                        LocalDateTime.of(dia, LocalTime.of(12, 0))), StatusReserva.CONFIRMADA);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));
        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(), dia)).thenReturn(List.of(reserva));

        boolean conflito = service.verificarDisponibilidade(sala.getId(), dia,
                LocalDateTime.of(dia, LocalTime.of(11, 0)),
                LocalDateTime.of(dia, LocalTime.of(13, 0)));

        assertThat(conflito).isFalse();

        boolean naoConflito = service.verificarDisponibilidade(sala.getId(), dia,
                LocalDateTime.of(dia, LocalTime.of(13, 0)), LocalDateTime.of(dia, LocalTime.of(14, 0)));

        assertThat(naoConflito).isTrue();
    }

    @Test
    @DisplayName("Deve recusar caso a sala não esteja cadastrada")
    void validarQuandoSalaNaoExiste() {

        UUID idInexistente = UUID.randomUUID();
        LocalDate dia = LocalDate.of(2020, 1, 1);

        when(salaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        boolean disponivel = service.verificarDisponibilidade(
                idInexistente, dia, LocalDateTime.of(dia, LocalTime.of(11, 0)), LocalDateTime.of(dia, LocalTime.of(13, 0)));

        assertThat(disponivel).isFalse();
    }

    @Test
    @DisplayName("Deve validar a edição da reserva")
    void validarEdicaoReserva() {

        Sala sala = new Sala("Lab 1", 50);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));


        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(),
                "solicitante",
                new PeriodoReserva(LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(12, 0)),
                        LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(13, 0))),
                StatusReserva.CONFIRMADA);

        when(reservaRepository.findById(reserva.getId()))
                .thenReturn(Optional.of(reserva));
        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(),
                LocalDate.of(2026, 10, 10))).thenReturn(List.of(reserva));

        Reserva reservaEditada = new Reserva(
                reserva.getId(),
                sala.getId(),
                reserva.getSolicitante(),
                new PeriodoReserva(LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(14, 0)),
                        LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(15, 0))),
                reserva.getStatus()
        );

        service.editarReserva(reservaEditada);

        verify(reservaRepository).update(reservaEditada);
    }

    @Test
    @DisplayName("Deve Informar conflito caso o período não esteja disponivel")
    void validarConflitoDeHorarioEdicao() {

        Sala sala = new Sala("Lab 1", 50);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));

        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(),
                "solicitante", new PeriodoReserva(LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(12, 0)),
                LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(13, 0))),
                StatusReserva.CONFIRMADA);

        Reserva reserva2 = new Reserva(UUID.randomUUID(), sala.getId(),
                "solicitante", new PeriodoReserva(LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(14, 0)),
                LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(17, 0))),
                StatusReserva.CONFIRMADA);

        LocalDate dia = reserva.getPeriodo().getInicio().toLocalDate();

        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(), dia))
                .thenReturn(List.of(reserva, reserva2));
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        Reserva reservaEditada = new Reserva(
                reserva.getId(),
                sala.getId(),
                reserva.getSolicitante(),
                new PeriodoReserva(LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(14, 0)),
                        LocalDateTime.of(LocalDate.of(2026, 10, 10), LocalTime.of(15, 0))),
                reserva.getStatus()
        );

        service.editarReserva(reservaEditada);

        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("deve rejeitar a edicao caso a reserva esteja cancelada")
    void validarEdicaoReservaCancelada() {

        Sala sala = new Sala("Lab 1", 50);

        Reserva reservaCancelada = new Reserva(
                UUID.randomUUID(),
                sala.getId(),
                "solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 12, 0),
                        LocalDateTime.of(2026, 10, 10, 13, 0)),
                StatusReserva.CANCELADA);

        when(reservaRepository.findById(reservaCancelada.getId()))
                .thenReturn(Optional.of(reservaCancelada));

        Reserva reservaEditada = new Reserva(
                reservaCancelada.getId(),
                sala.getId(),
                "outro solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 14, 0),
                        LocalDateTime.of(2026, 10, 10, 15, 0)),
                StatusReserva.CONFIRMADA);

        assertThatThrownBy(() -> service.editarReserva(reservaEditada))
                .isInstanceOf(ReservaCanceladaException.class);

        assertThat(reservaCancelada.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(reservaCancelada.getSolicitante()).isEqualTo("solicitante");
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("deve rejeitar se a reserva nao existir")
    void validarEdicaoReservaInexistente() {

        Sala sala = new Sala("Lab 1", 50);

        Reserva reservaEditada = new Reserva(
                UUID.randomUUID(),
                sala.getId(),
                "solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 12, 0),
                        LocalDateTime.of(2026, 10, 10, 13, 0)),
                StatusReserva.CONFIRMADA);

        when(reservaRepository.findById(reservaEditada.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editarReserva(reservaEditada))
                .isInstanceOf(ReservaNaoEncontradaException.class);

        verify(reservaRepository).findById(reservaEditada.getId());
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("deve excluir o participante existente")
    void validarRemocaoParticipanteExistente() {

        Participante pessoa1 = new Participante("pessoa1");
        Participante pessoa2 = new Participante("pessoa2");
        List<Participante> participantes = new ArrayList<>(List.of(pessoa1, pessoa2));

        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 14, 0),
                        LocalDateTime.of(2026, 10, 10, 15, 0)),
                StatusReserva.CONFIRMADA,
                participantes
        );

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        service.excluirParticipante(reserva.getId(), pessoa1);

        assertThat(reserva.getParticipantes()).containsExactly(pessoa2);
        verify(reservaRepository).update(reserva);
    }


    @Test
    @DisplayName("deve rejeitar ao tentar remover um participante que não está na reserva")
    void validarRemocaoParticipanteInexistente() {

        Participante pessoa1 = new Participante("pessoa1");
        Participante pessoa2 = new Participante("pessoa2");
        Participante pessoa3 = new Participante("pessoa3");

        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 14, 0),
                        LocalDateTime.of(2026, 10, 10, 15, 0)),
                StatusReserva.CONFIRMADA,
                new ArrayList<>(List.of(pessoa1, pessoa2))
        );

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.excluirParticipante(reserva.getId(), pessoa3))
                .isInstanceOf(ParticipanteNaoEncontradoException.class);

        assertThat(reserva.getParticipantes()).containsExactly(pessoa1,pessoa2);

        verify(reservaRepository,never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("deve rejeitar a remoção de participante quando a reserva está cancelada")
    void validarRemocaoParticipanteReservaCancelada() {
        Participante pessoa1 = new Participante("pessoa1");
        Participante pessoa2 = new Participante("pessoa2");

        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "solicitante",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 14, 0),
                        LocalDateTime.of(2026, 10, 10, 15, 0)),
                StatusReserva.CANCELADA,
                new ArrayList<>(List.of(pessoa1, pessoa2))
        );

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.excluirParticipante(reserva.getId(), pessoa2))
                .isInstanceOf(ReservaCanceladaException.class);

        assertThat(reserva.getParticipantes()).containsExactly(pessoa1,pessoa2);

        verify(reservaRepository,never()).update(any(Reserva.class));

    }


}

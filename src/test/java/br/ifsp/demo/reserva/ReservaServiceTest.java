package br.ifsp.demo.reserva;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.repository.ReservaRepository;
import br.ifsp.demo.repository.SalaRepository;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReservaServiceTest {
    @Test
    @DisplayName("Sala disponível quando não há reservas no período informado")
    void ValidaVerificacaodeDisponibilidade(){
        SalaRepository salaRepository = mock(SalaRepository.class);
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        ReservaService service = new ReservaService(salaRepository, reservaRepository);

        Sala sala = new Sala("Lab 1",50);
        LocalDate dia = LocalDate.of(2020, 1, 1);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));
        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(), dia))
                .thenReturn(List.of());

        boolean disponivel = service.verificarDisponibilidade(sala.getId(),dia,
                LocalTime.of(10,0),LocalTime.of(12,0));

        assertThat(disponivel).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar quando o período na sala já está alugado")
    void ValidarConflitoDeHorario() {
        SalaRepository salaRepository = mock(SalaRepository.class);
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        ReservaService service = new ReservaService(salaRepository, reservaRepository);

        Sala sala = new Sala("Lab 1", 50);
        LocalDate dia = LocalDate.of(2020, 1, 1);

        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(), "solicitante",
                new PeriodoReserva(LocalTime.of(10, 0), LocalTime.of(12, 0)), StatusReserva.CONFIRMADA);

        when(salaRepository.findById(sala.getId())).thenReturn(Optional.of(sala));
        when(reservaRepository.findConfirmadasPorSalaEDia(sala.getId(), dia)).thenReturn(List.of(reserva));

        boolean conflito = service.verificarDisponibilidade(sala.getId(), dia, LocalTime.of(11, 0), LocalTime.of(13, 0));

        assertThat(conflito).isFalse();

        boolean naoConflito = service.verificarDisponibilidade(sala.getId(), dia, LocalTime.of(13, 0), LocalTime.of(14, 0));

        assertThat(naoConflito).isTrue();
    }
}

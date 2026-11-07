package br.ifsp.demo.sala.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import br.ifsp.demo.sala.exception.NomeEmUsoException;
import br.ifsp.demo.sala.service.SalaService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SalaServiceStructuralTest {

    private final SalaRepository salaRepository = mock(SalaRepository.class);
    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final SalaService service = new SalaService(salaRepository, reservaRepository);

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCadastrarSalaQuandoNomeNaoEstaEmUso() {
        when(salaRepository.existePorNome("Sala 01")).thenReturn(false);
        when(salaRepository.salvar(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala resultado = service.cadastrarSala("Sala 01", 10);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Sala 01");
        assertThat(resultado.getCapacidade()).isEqualTo(10);
        verify(salaRepository).salvar(resultado);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarExcecaoAoCadastrarSalaComNomeEmUso() {
        when(salaRepository.existePorNome("Sala 01")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarSala("Sala 01", 10))
                .isInstanceOf(NomeEmUsoException.class);

        verify(salaRepository, never()).salvar(any(Sala.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarExcecaoAoEditarSalaInexistente() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editarSala(salaId, "Novo Nome", 5))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessage("sala não foi encontrada.");

        verifyNoInteractions(reservaRepository);
        verify(salaRepository, never()).salvar(any(Sala.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarExcecaoAoEditarSalaQuandoReservaFuturaExcedeNovaCapacidade() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(new Sala(salaId, "Sala 01", 10)));
        when(reservaRepository.buscarFuturasConfirmadasPorSala(eq(salaId), any(LocalDateTime.class)))
                .thenReturn(List.of(reservaComParticipantes(salaId, 3)));

        assertThatThrownBy(() -> service.editarSala(salaId, "Sala 01", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conflito com reservas existentes");

        verify(salaRepository, never()).salvar(any(Sala.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveEditarSalaQuandoNaoHaReservasFuturas() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(new Sala(salaId, "Sala 01", 10)));
        when(reservaRepository.buscarFuturasConfirmadasPorSala(eq(salaId), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(salaRepository.salvar(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala resultado = service.editarSala(salaId, "Sala Nova", 20);

        ArgumentCaptor<Sala> captor = ArgumentCaptor.forClass(Sala.class);
        verify(salaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(salaId);
        assertThat(captor.getValue().getNome()).isEqualTo("Sala Nova");
        assertThat(captor.getValue().getCapacidade()).isEqualTo(20);
        assertThat(resultado).isSameAs(captor.getValue());
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveEditarSalaQuandoParticipantesDaReservaFuturaIgualamNovaCapacidade() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(new Sala(salaId, "Sala 01", 10)));
        when(reservaRepository.buscarFuturasConfirmadasPorSala(eq(salaId), any(LocalDateTime.class)))
                .thenReturn(List.of(reservaComParticipantes(salaId, 3)));
        when(salaRepository.salvar(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala resultado = service.editarSala(salaId, "Sala 01", 3);

        assertThat(resultado.getCapacidade()).isEqualTo(3);
        verify(salaRepository).salvar(resultado);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarExcecaoAoRemoverSalaInexistente() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removerSala(salaId))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessage("sala não foi encontrada.");

        verifyNoInteractions(reservaRepository);
        verify(salaRepository, never()).remover(any(UUID.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarExcecaoAoRemoverSalaComReservasPendentes() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(new Sala(salaId, "Sala 01", 10)));
        when(reservaRepository.buscarFuturasConfirmadasPorSala(eq(salaId), any(LocalDateTime.class)))
                .thenReturn(List.of(reservaComParticipantes(salaId, 1)));

        assertThatThrownBy(() -> service.removerSala(salaId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sala possui reservas pendentes e não pode ser removida.");

        verify(salaRepository, never()).remover(any(UUID.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRemoverSalaQuandoNaoHaReservasPendentes() {
        UUID salaId = UUID.randomUUID();
        when(salaRepository.buscarPorId(salaId)).thenReturn(Optional.of(new Sala(salaId, "Sala 01", 10)));
        when(reservaRepository.buscarFuturasConfirmadasPorSala(eq(salaId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.removerSala(salaId);

        verify(salaRepository).remover(salaId);
    }

    private Reserva reservaComParticipantes(UUID salaId, int quantidade) {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                salaId,
                "Pedro",
                new PeriodoReserva(inicio, inicio.plusHours(1)),
                StatusReserva.CONFIRMADA);
        for (int i = 1; i <= quantidade; i++) {
            reserva.adicionarParticipante(new Participante("Participante " + i));
        }
        return reserva;
    }
}

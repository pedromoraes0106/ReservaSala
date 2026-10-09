package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.PeriodoInvalidoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservaServiceTddTest {

    private ReservaService service;
    private final Map<UUID, Sala> salas = new ConcurrentHashMap<>();
    private final Map<UUID, Reserva> reservas = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        SalaRepository salaRepository = new SalaRepository() {
            @Override
            public Optional<Sala> buscarPorId(UUID id) {
                return Optional.ofNullable(salas.get(id));
            }

            @Override
            public Sala salvar(Sala sala) {
                salas.put(sala.getId(), sala);
                return sala;
            }
        };

        ReservaRepository reservaRepository = new ReservaRepository() {
            @Override
            public Optional<Reserva> buscarPorId(UUID reservaId) {
                return Optional.ofNullable(reservas.get(reservaId));
            }

            @Override
            public List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim) {
                return reservas.values().stream()
                        .filter(reserva -> reserva.getSalaId().equals(salaId))
                        .filter(reserva -> reserva.getStatus() == StatusReserva.CONFIRMADA)
                        .filter(reserva -> reserva.getPeriodo().temSobreposicaoCom(inicio, fim))
                        .toList();
            }

            @Override
            public List<Reserva> buscarFuturasConfirmadasPorSala(UUID salaId, LocalDateTime aPartirDe) {
                return reservas.values().stream()
                        .filter(reserva -> reserva.getSalaId().equals(salaId))
                        .filter(reserva -> reserva.getStatus() == StatusReserva.CONFIRMADA)
                        .filter(reserva -> reserva.getPeriodo().getInicio().isAfter(aPartirDe))
                        .toList();
            }

            @Override
            public Reserva salvar(Reserva reserva) {
                reservas.put(reserva.getId(), reserva);
                return reserva;
            }

            @Override
            public List<Reserva> buscarPorSolicitante(String solicitante) {
                return reservas.values().stream()
                        .filter(reserva -> reserva.getSolicitante().equals(solicitante))
                        .toList();
            }
        };

        service = new ReservaService(salaRepository, reservaRepository);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveCriarReservaQuandoSalaDisponivel() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(reserva.getSolicitante()).isEqualTo("Pedro");
        assertThat(reserva.getSalaId()).isEqualTo(sala.getId());
        assertThat(reserva.getPeriodo()).isEqualTo(periodo);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveAdicionarParticipanteQuandoReservaConfirmada() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);

        service.adicionarParticipante(reserva.getId(), "Maria");

        assertThat(reserva.getParticipantes())
            .extracting(Participante::getNome)
            .containsExactly("Maria");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarParticipanteDuplicado() {
        Reserva reserva = new Reserva(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Pedro",
            new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
            ),
            StatusReserva.CONFIRMADA
        );
        Participante participante = new Participante("Maria");
        reserva.adicionarParticipante(participante);

        assertThatThrownBy(() -> reserva.adicionarParticipante(participante))
            .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está na reserva");
        assertThat(reserva.getParticipantes()).containsExactly(participante);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveCancelarReservaAtiva() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);

        Reserva reservaCancelada = service.cancelarReserva(reserva.getId());

        assertThat(reservaCancelada.getStatus()).isEqualTo(StatusReserva.CANCELADA);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCancelamentoDeReservaJaCancelada() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reservaCancelada = new Reserva(
                UUID.randomUUID(),
                sala.getId(),
                "Pedro",
                periodo,
                StatusReserva.CANCELADA
        );
        reservas.put(reservaCancelada.getId(), reservaCancelada);

        assertThatThrownBy(() -> service.cancelarReserva(reservaCancelada.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está cancelada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCancelamentoDeReservaInexistente() {
        UUID reservaIdInexistente = UUID.randomUUID();

        assertThatThrownBy(() -> service.cancelarReserva(reservaIdInexistente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não foi encontrada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarParticipanteEmReservaCancelada() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reservaCancelada = new Reserva(
                UUID.randomUUID(),
                sala.getId(),
                "Pedro",
                periodo,
                StatusReserva.CANCELADA
        );
        reservas.put(reservaCancelada.getId(), reservaCancelada);

        assertThatThrownBy(() -> service.adicionarParticipante(reservaCancelada.getId(), "Maria"))
                .isInstanceOf(ReservaCanceladaException.class)
                .hasMessageContaining("não está mais ativa");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarPeriodoInvalido() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        assertThatThrownBy(() -> new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 12, 0),
                LocalDateTime.of(2026, 10, 10, 9, 0)
        ))
                .isInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("período inválido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarSalaInexistente() {
        UUID idInexistente = UUID.randomUUID();

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        assertThatThrownBy(() -> service.criarReserva(idInexistente, "Pedro", periodo))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessageContaining("não existe");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveListarTodasAsReservasDoSolicitante() {
        UUID salaId = UUID.randomUUID();

        PeriodoReserva periodo1 = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 10, 0)
        );

        PeriodoReserva periodo2 = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 11, 14, 0),
                LocalDateTime.of(2026, 10, 11, 15, 0)
        );

        Reserva reserva1 = new Reserva(
                UUID.randomUUID(), salaId, "Pedro",
                periodo1, StatusReserva.CONFIRMADA
        );

        Reserva reserva2 = new Reserva(
                UUID.randomUUID(), salaId, "Pedro",
                periodo2, StatusReserva.CONFIRMADA
        );

        Reserva reservaDeOutroSolicitante = new Reserva(
                UUID.randomUUID(), salaId, "Maria",
                periodo1, StatusReserva.CONFIRMADA
        );

        reservas.put(reserva1.getId(), reserva1);
        reservas.put(reserva2.getId(), reserva2);
        reservas.put(
                reservaDeOutroSolicitante.getId(),
                reservaDeOutroSolicitante
        );

        List<Reserva> resultado = service.listarPorSolicitante("Pedro");

        assertThat(resultado)
                .containsExactlyInAnyOrder(reserva1, reserva2);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRetornarListaVaziaQuandoSolicitanteNaoPossuiReservas() {
        List<Reserva> resultado =
                service.listarPorSolicitante("Carlos");

        assertThat(resultado).isEmpty();
    }
}

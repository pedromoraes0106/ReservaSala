package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.PeriodoInvalidoException;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalDate;
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

            @Override
            public void remover(UUID id) {
                salas.remove(id);
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
            public List<Reserva> buscarComFiltros(UUID salaId, LocalDateTime inicio, LocalDateTime fim, String solicitante) {
                return reservas.values().stream()
                        .filter(reserva -> salaId == null || reserva.getSalaId().equals(salaId))
                        .filter(reserva -> solicitante == null || solicitante.isBlank() || reserva.getSolicitante().equals(solicitante))
                        .filter(reserva -> inicio == null || fim == null || reserva.getPeriodo().temSobreposicaoCom(inicio, fim))
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
    void deveInformarDisponibilidadeQuandoNaoHaConflito() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala disponível", 8);
        service.getSalaRepository().salvar(sala);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 9, 0);
        LocalDateTime fim = inicio.plusHours(1);

        boolean disponivel = service.verificarDisponibilidade(
                sala.getId(), inicio.toLocalDate(), inicio, fim);

        assertThat(disponivel).isTrue();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveIndicarIndisponibilidadeQuandoHaConflitoDeHorario() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala ocupada", 8);
        service.getSalaRepository().salvar(sala);
        LocalDateTime inicioReserva = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reserva reserva = service.criarReserva(
                sala.getId(), "Pedro", new PeriodoReserva(inicioReserva, inicioReserva.plusHours(2)));

        boolean disponivel = service.verificarDisponibilidade(
                sala.getId(), inicioReserva.toLocalDate(), inicioReserva.plusHours(1), inicioReserva.plusHours(3));

        assertThat(disponivel).isFalse();
    }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        void deveRejeitarConsultaDeDisponibilidadeParaSalaInexistente() {
                LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 9, 0);

                assertThatThrownBy(() -> service.verificarDisponibilidade(
                                UUID.randomUUID(), inicio.toLocalDate(), inicio, inicio.plusHours(1)))
                                .isInstanceOf(SalaNaoEncontradaException.class)
                                .hasMessageContaining("sala não existe");
        }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarEdicaoDeReservaInexistente() {
        UUID reservaId = UUID.randomUUID();
        Reserva reservaEditada = new Reserva(
                reservaId,
                UUID.randomUUID(),
                "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CONFIRMADA
        );

        assertThatThrownBy(() -> service.editarReserva(reservaEditada))
                .isInstanceOf(ReservaNaoEncontradaException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveEditarReservaMantendoOId() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala edição", 8);
        service.getSalaRepository().salvar(sala);
        LocalDateTime inicioOriginal = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reserva reserva = service.criarReserva(
                sala.getId(), "Pedro", new PeriodoReserva(inicioOriginal, inicioOriginal.plusHours(1)));
        LocalDateTime novoInicio = inicioOriginal.plusHours(2);
        Reserva alteracoes = new Reserva(
                reserva.getId(), sala.getId(), "Maria",
                new PeriodoReserva(novoInicio, novoInicio.plusHours(1)), StatusReserva.CONFIRMADA);

        service.editarReserva(alteracoes);

        assertThat(reserva.getId()).isEqualTo(alteracoes.getId());
        assertThat(reserva.getSolicitante()).isEqualTo("Maria");
        assertThat(reserva.getPeriodo()).isEqualTo(alteracoes.getPeriodo());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveManterReservaOriginalQuandoEdicaoCriaConflito() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala conflito", 8);
        service.getSalaRepository().salvar(sala);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reserva reserva = service.criarReserva(
                sala.getId(), "Pedro", new PeriodoReserva(inicio, inicio.plusHours(1)));
        service.criarReserva(sala.getId(), "Maria", new PeriodoReserva(inicio.plusHours(2), inicio.plusHours(3)));
        Reserva alteracoes = new Reserva(
                reserva.getId(), sala.getId(), "Pedro",
                new PeriodoReserva(inicio.plusHours(1).plusMinutes(30), inicio.plusHours(2).plusMinutes(30)),
                StatusReserva.CONFIRMADA);

        assertThatThrownBy(() -> service.editarReserva(alteracoes))
                .isInstanceOf(br.ifsp.demo.reserva.exception.ConflitoDeHorarioException.class)
                .hasMessageContaining("conflito");

        assertThat(reserva.getPeriodo()).isEqualTo(new PeriodoReserva(inicio, inicio.plusHours(1)));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRemoverParticipantePeloNome() {
        Participante participante = new Participante("Maria");
        Reserva reserva = new Reserva(
                UUID.randomUUID(), UUID.randomUUID(), "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CONFIRMADA, List.of(participante));
        reservas.put(reserva.getId(), reserva);

        service.excluirParticipante(reserva.getId(), "Maria");

        assertThat(reserva.getParticipantes()).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarRemocaoPeloNomeDeParticipanteInexistente() {
        Reserva reserva = new Reserva(
                UUID.randomUUID(), UUID.randomUUID(), "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CONFIRMADA);
        reservas.put(reserva.getId(), reserva);

        assertThatThrownBy(() -> service.excluirParticipante(reserva.getId(), "Maria"))
                .isInstanceOf(ParticipanteNaoEncontradoException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRemoverParticipanteExistente() {
        Participante participante = new Participante("Maria");
        Reserva reserva = new Reserva(
                UUID.randomUUID(), UUID.randomUUID(), "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CONFIRMADA, List.of(participante));
        reservas.put(reserva.getId(), reserva);

        service.excluirParticipante(reserva.getId(), participante);

        assertThat(reserva.getParticipantes()).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarRemocaoDeParticipanteInexistente() {
        Reserva reserva = new Reserva(
                UUID.randomUUID(), UUID.randomUUID(), "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CONFIRMADA);
        reservas.put(reserva.getId(), reserva);

        assertThatThrownBy(() -> service.excluirParticipante(reserva.getId(), new Participante("Maria")))
                .isInstanceOf(ParticipanteNaoEncontradoException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarRemocaoDeParticipanteDeReservaCancelada() {
        Participante participante = new Participante("Maria");
        Reserva reservaCancelada = new Reserva(
                UUID.randomUUID(), UUID.randomUUID(), "Pedro",
                new PeriodoReserva(LocalDateTime.of(2026, 10, 20, 9, 0), LocalDateTime.of(2026, 10, 20, 10, 0)),
                StatusReserva.CANCELADA, List.of(participante));
        reservas.put(reservaCancelada.getId(), reservaCancelada);

        assertThatThrownBy(() -> service.excluirParticipante(reservaCancelada.getId(), participante))
                .isInstanceOf(ReservaCanceladaException.class);
        assertThat(reservaCancelada.getParticipantes()).containsExactly(participante);
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
    void deveConfirmarCheckInQuandoReservaEstaDentroDoPeriodo() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);

        Reserva reservaEmUso = service.confirmarCheckIn(reserva.getId(), LocalDateTime.of(2026, 10, 10, 10, 30));

        assertThat(reservaEmUso.getStatus()).isEqualTo(StatusReserva.EM_USO);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCheckInForaDoPeriodoDaReserva() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        service.getSalaRepository().salvar(sala);

        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 10, 10, 9, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0)
        );

        Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);

        assertThatThrownBy(() -> service.confirmarCheckIn(reserva.getId(), LocalDateTime.of(2026, 10, 10, 12, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dentro do período reservado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCheckInDeReservaCancelada() {
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

        assertThatThrownBy(() -> service.confirmarCheckIn(reservaCancelada.getId(), LocalDateTime.of(2026, 10, 10, 10, 30)))
                .isInstanceOf(ReservaCanceladaException.class)
                .hasMessageContaining("não está mais ativa");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarParticipanteDuplicado() {
                Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
                service.getSalaRepository().salvar(sala);

                PeriodoReserva periodo = new PeriodoReserva(
                                LocalDateTime.of(2026, 10, 10, 9, 0),
                                LocalDateTime.of(2026, 10, 10, 11, 0)
                );

                Reserva reserva = service.criarReserva(sala.getId(), "Pedro", periodo);
                service.adicionarParticipante(reserva.getId(), "Maria");

                assertThatThrownBy(() -> service.adicionarParticipante(reserva.getId(), "Maria"))
                                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está na reserva");
                assertThat(reserva.getParticipantes())
                                .extracting(Participante::getNome)
                                .containsExactly("Maria");
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
                .isInstanceOf(ReservaCanceladaException.class)
                .hasMessageContaining("já está cancelada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCancelamentoDeReservaInexistente() {
        UUID reservaIdInexistente = UUID.randomUUID();

        assertThatThrownBy(() -> service.cancelarReserva(reservaIdInexistente))
                .isInstanceOf(ReservaNaoEncontradaException.class)
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

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveConsultarReservasFiltrandoPorSalaEPeriodo() {
        UUID salaA = UUID.randomUUID();
        UUID salaB = UUID.randomUUID();

        Reserva reservaSalaA = new Reserva(
                UUID.randomUUID(),
                salaA,
                "Pedro",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 10, 9, 0),
                        LocalDateTime.of(2026, 10, 10, 10, 0)
                ),
                StatusReserva.CONFIRMADA
        );

        Reserva reservaSalaB = new Reserva(
                UUID.randomUUID(),
                salaB,
                "Maria",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 11, 14, 0),
                        LocalDateTime.of(2026, 10, 11, 15, 0)
                ),
                StatusReserva.CONFIRMADA
        );

        reservas.put(reservaSalaA.getId(), reservaSalaA);
        reservas.put(reservaSalaB.getId(), reservaSalaB);

        List<Reserva> resultado = service.consultarReservas(
                salaA,
                LocalDateTime.of(2026, 10, 10, 0, 0),
                LocalDateTime.of(2026, 10, 10, 23, 59),
                null
        );

        assertThat(resultado).containsExactly(reservaSalaA);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveConsultarReservasFiltrandoPorSolicitante() {
        UUID salaA = UUID.randomUUID();
        UUID salaB = UUID.randomUUID();

        Reserva reservaPedro = new Reserva(
                UUID.randomUUID(),
                salaA,
                "Pedro",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 12, 9, 0),
                        LocalDateTime.of(2026, 10, 12, 10, 0)
                ),
                StatusReserva.CONFIRMADA
        );

        Reserva reservaMaria = new Reserva(
                UUID.randomUUID(),
                salaB,
                "Maria",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 12, 11, 0),
                        LocalDateTime.of(2026, 10, 12, 12, 0)
                ),
                StatusReserva.CONFIRMADA
        );

        reservas.put(reservaPedro.getId(), reservaPedro);
        reservas.put(reservaMaria.getId(), reservaMaria);

        List<Reserva> resultado = service.consultarReservas(
                null,
                null,
                null,
                "Pedro"
        );

        assertThat(resultado).containsExactly(reservaPedro);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRetornarListaVaziaQuandoNaoHaReservasQueAtendamAoFiltro() {
        UUID salaA = UUID.randomUUID();

        Reserva reservaOutra = new Reserva(
                UUID.randomUUID(),
                salaA,
                "Maria",
                new PeriodoReserva(
                        LocalDateTime.of(2026, 10, 15, 9, 0),
                        LocalDateTime.of(2026, 10, 15, 10, 0)
                ),
                StatusReserva.CONFIRMADA
        );

        reservas.put(reservaOutra.getId(), reservaOutra);

        List<Reserva> resultado = service.consultarReservas(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 20, 0, 0),
                LocalDateTime.of(2026, 10, 20, 23, 59),
                "Pedro"
        );

        assertThat(resultado).isEmpty();
    }
}

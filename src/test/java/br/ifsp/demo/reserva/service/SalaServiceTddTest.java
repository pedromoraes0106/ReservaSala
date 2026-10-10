package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import br.ifsp.demo.reserva.repository.JdbcReservaRepository;
import br.ifsp.demo.reserva.repository.ReservaRepository;
import br.ifsp.demo.reserva.repository.SalaRepository;
import br.ifsp.demo.sala.service.SalaService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SalaServiceTddTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveCadastrarSalaComDadosValidos() {
        Map<UUID, Sala> salas = new HashMap<>();
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

            @Override
            public boolean existePorNome(String nome) {
                return salas.values().stream()
                        .anyMatch(salaCadastrada -> salaCadastrada.getNome().equals(nome));
            }
        };
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of()));

        Sala salaCadastrada = service.cadastrarSala("Sala nova", 12);

        assertThat(salaCadastrada.getNome()).isEqualTo("Sala nova");
        assertThat(salaCadastrada.getCapacidade()).isEqualTo(12);
        assertThat(salaRepository.buscarPorId(salaCadastrada.getId())).contains(salaCadastrada);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarCadastroDeSalaComNomeDuplicado() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala existente", 8);
        SalaRepository salaRepository = criarSalaRepository(sala);
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of()));

        assertThatThrownBy(() -> service.cadastrarSala("Sala existente", 10))
                .isInstanceOf(br.ifsp.demo.sala.exception.NomeEmUsoException.class)
                .hasMessageContaining("Sala existente");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveAtualizarInformacoesDaSalaExistente() {
        Map<UUID, Sala> salas = new HashMap<>();
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
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of()));
        UUID salaId = UUID.randomUUID();
        salaRepository.salvar(new Sala(salaId, "Sala antiga", 8));

        Sala salaAtualizada = service.editarSala(salaId, "Sala renovada", 12);

        assertThat(salaAtualizada.getId()).isEqualTo(salaId);
        assertThat(salaAtualizada.getNome()).isEqualTo("Sala renovada");
        assertThat(salaAtualizada.getCapacidade()).isEqualTo(12);
        assertThat(salaRepository.buscarPorId(salaId)).contains(salaAtualizada);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarRemocaoDeSalaComReservaFuturaConfirmada() {
        UUID salaId = UUID.randomUUID();
        Sala sala = new Sala(salaId, "Sala com reserva", 8);
        Reserva reservaFutura = new Reserva(
                UUID.randomUUID(),
                salaId,
                "Pedro",
                new PeriodoReserva(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)),
                StatusReserva.CONFIRMADA,
                List.of(new Participante("Maria"))
        );

        SalaService service = new SalaService(criarSalaRepository(sala), criarReservaRepository(List.of(reservaFutura)));

        assertThatThrownBy(() -> service.removerSala(salaId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reservas pendentes");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarRemocaoDeSalaInexistente() {
        SalaRepository salaRepository = new SalaRepository() {
            @Override
            public Optional<Sala> buscarPorId(UUID id) {
                return Optional.empty();
            }

            @Override
            public Sala salvar(Sala sala) {
                return sala;
            }

            @Override
            public void remover(UUID id) {
            }
        };
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of()));

        assertThatThrownBy(() -> service.removerSala(UUID.randomUUID()))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessageContaining("sala não foi encontrada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarEdicaoDeSalaInexistente() {
        SalaRepository salaRepository = new SalaRepository() {
            @Override
            public Optional<Sala> buscarPorId(UUID id) {
                return Optional.empty();
            }

            @Override
            public Sala salvar(Sala sala) {
                return sala;
            }

            @Override
            public void remover(UUID id) {
            }
        };
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of()));

        assertThatThrownBy(() -> service.editarSala(UUID.randomUUID(), "Sala nova", 12))
                .isInstanceOf(SalaNaoEncontradaException.class)
                .hasMessageContaining("sala não foi encontrada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveRejeitarReducaoDeCapacidadeAbaixoDeReservasConfirmadas() {
        UUID salaId = UUID.randomUUID();
        Sala sala = new Sala(salaId, "Sala atual", 8);
        PeriodoReserva periodoFuturo = new PeriodoReserva(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );
        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                salaId,
                "Pedro",
                periodoFuturo,
                StatusReserva.CONFIRMADA,
                List.of(new Participante("Maria"), new Participante("Joao"), new Participante("Ana"))
        );
        SalaRepository salaRepository = criarSalaRepository(sala);
        SalaService service = new SalaService(salaRepository, criarReservaRepository(List.of(reserva)));

        assertThatThrownBy(() -> service.editarSala(salaId, "Sala atualizada", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conflito com reservas existentes");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void devePersistirParticipantesParaConsultaDeCapacidade() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        try {
            new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            UUID salaId = UUID.randomUUID();
            jdbcTemplate.update("INSERT INTO sala (id, nome, capacidade) VALUES (?, ?, ?)", salaId.toString(), "Sala", 8);
            LocalDateTime agora = LocalDateTime.now();
            ReservaRepository reservaRepository = new JdbcReservaRepository(jdbcTemplate);
            Reserva reserva = new Reserva(
                    UUID.randomUUID(),
                    salaId,
                    "Pedro",
                    new PeriodoReserva(agora.plusDays(1), agora.plusDays(1).plusHours(1)),
                    StatusReserva.CONFIRMADA,
                    List.of(new Participante("Maria"), new Participante("Joao"), new Participante("Ana"))
            );

            reservaRepository.salvar(reserva);

            List<Reserva> reservasFuturas = reservaRepository.buscarFuturasConfirmadasPorSala(salaId, agora);
            assertThat(reservasFuturas).hasSize(1);
            assertThat(reservasFuturas.getFirst().getParticipantes()).hasSize(3);
        } finally {
            dataSource.destroy();
        }
    }

    private SalaRepository criarSalaRepository(Sala salaInicial) {
        Map<UUID, Sala> salas = new HashMap<>();
        salas.put(salaInicial.getId(), salaInicial);
        return new SalaRepository() {
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

            @Override
            public boolean existePorNome(String nome) {
                return salas.values().stream()
                        .anyMatch(salaCadastrada -> salaCadastrada.getNome().equals(nome));
            }
        };
    }

    private ReservaRepository criarReservaRepository(List<Reserva> reservas) {
        return new ReservaRepository() {
            @Override
            public Optional<Reserva> buscarPorId(UUID reservaId) {
                return reservas.stream().filter(reserva -> reserva.getId().equals(reservaId)).findFirst();
            }

            @Override
            public List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim) {
                return reservas.stream()
                        .filter(reserva -> reserva.getSalaId().equals(salaId))
                        .filter(reserva -> reserva.getPeriodo().temSobreposicaoCom(inicio, fim))
                        .toList();
            }

            @Override
            public List<Reserva> buscarComFiltros(UUID salaId, LocalDateTime inicio, LocalDateTime fim, String solicitante) {
                return reservas.stream()
                        .filter(reserva -> salaId == null || reserva.getSalaId().equals(salaId))
                        .filter(reserva -> solicitante == null || solicitante.isBlank() || reserva.getSolicitante().equals(solicitante))
                        .filter(reserva -> inicio == null || fim == null || reserva.getPeriodo().temSobreposicaoCom(inicio, fim))
                        .toList();
            }

            @Override
            public List<Reserva> buscarFuturasConfirmadasPorSala(UUID salaId, LocalDateTime aPartirDe) {
                return reservas.stream()
                        .filter(reserva -> reserva.getSalaId().equals(salaId))
                        .filter(reserva -> reserva.getStatus() == StatusReserva.CONFIRMADA)
                        .filter(reserva -> reserva.getPeriodo().getInicio().isAfter(aPartirDe))
                        .toList();
            }

            @Override
            public Reserva salvar(Reserva reserva) {
                return reserva;
            }

            @Override
            public List<Reserva> buscarPorSolicitante(String solicitante) {
                return reservas.stream()
                        .filter(reserva -> reserva.getSolicitante().equals(solicitante))
                        .toList();
            }
        };
    }
}
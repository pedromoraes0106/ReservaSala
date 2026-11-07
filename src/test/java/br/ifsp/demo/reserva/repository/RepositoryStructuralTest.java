package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryStructuralTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 6, 9, 0);
    private static final LocalDateTime FIM = INICIO.plusHours(1);

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveListarTodasAsSalas() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), salaMapper()))
                .thenAnswer(invocation -> {
                    RowMapper<Sala> mapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.getString("id")).thenReturn(salaId.toString());
                    when(rs.getString("nome")).thenReturn("Sala 01");
                    when(rs.getInt("capacidade")).thenReturn(10);
                    return List.of(Objects.requireNonNull(mapper.mapRow(rs, 0)));
                });
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        List<Sala> resultado = repository.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(salaId);
        assertThat(resultado.getFirst().getNome()).isEqualTo("Sala 01");
        assertThat(resultado.getFirst().getCapacidade()).isEqualTo(10);
        verify(jdbcTemplate).query(contains("ORDER BY nome"), salaMapper());
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarVazioQuandoBuscaPorIdNaoEncontraReserva() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        Optional<Reserva> resultado = repository.buscarPorId(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarListaVaziaNoMetodoPadraoDeListarTodas() {
        SalaRepository repository = new SalaRepository() {
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

        assertThat(repository.listarTodas()).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCarregarReservaEParticipantesQuandoBuscaPorIdEncontraReserva() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = jdbcQueMapeiaReserva(reservaId, salaId, true);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        Optional<Reserva> resultado = repository.buscarPorId(reservaId);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(reservaId);
        assertThat(resultado.get().getSalaId()).isEqualTo(salaId);
        assertThat(resultado.get().getSolicitante()).isEqualTo("Pedro");
        assertThat(resultado.get().getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(resultado.get().getParticipantes()).extracting(Participante::getNome).containsExactly("Maria");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveBuscarReservasPorSalaEPeriodo() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = jdbcQueMapeiaReserva(UUID.randomUUID(), salaId, false);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarPorSalaEPeriodo(salaId, INICIO, FIM);

        assertThat(resultado).hasSize(1);
        verify(jdbcTemplate).query(
                contains("status = 'CONFIRMADA'"),
                reservaMapper(),
                eq(salaId.toString()),
                eq(FIM.toString()),
                eq(INICIO.toString()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveMontarConsultaSemFiltrosOpcionais() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarComFiltros(null, null, null, null);

        assertThat(resultado).isEmpty();
        verify(jdbcTemplate).query(eq("SELECT * FROM reserva WHERE 1 = 1"), reservaMapper(), any(Object[].class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveMontarConsultaComTodosOsFiltros() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        repository.buscarComFiltros(salaId, INICIO, FIM, "Pedro");

        verify(jdbcTemplate).query(
                contains("sala_id = ?"),
                reservaMapper(),
                eq(salaId.toString()),
                eq("Pedro"),
                eq(FIM.toString()),
                eq(INICIO.toString()));
        verify(jdbcTemplate).query(contains("solicitante = ?"), reservaMapper(), any(Object[].class));
        verify(jdbcTemplate).query(contains("inicio < ? AND fim > ?"), reservaMapper(), any(Object[].class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveIgnorarSolicitanteEmBrancoEPeriodoIncompleto() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        repository.buscarComFiltros(null, INICIO, null, "   ");
        repository.buscarComFiltros(null, null, FIM, "");

        verify(jdbcTemplate, atLeastOnce()).query(
                eq("SELECT * FROM reserva WHERE 1 = 1"),
                reservaMapper(),
                any(Object[].class));
        verify(jdbcTemplate, never()).query(contains("solicitante = ?"), reservaMapper(), any(Object[].class));
        verify(jdbcTemplate, never()).query(contains("inicio < ?"), reservaMapper(), any(Object[].class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveBuscarReservasFuturasConfirmadasPorSala() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarFuturasConfirmadasPorSala(salaId, INICIO);

        assertThat(resultado).isEmpty();
        verify(jdbcTemplate).query(
                contains("inicio > ?"),
                reservaMapper(),
                eq(salaId.toString()),
                eq(INICIO.toString()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveBuscarReservasPorSolicitante() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), reservaMapper(), any(Object[].class))).thenReturn(List.of());
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarPorSolicitante("Pedro");

        assertThat(resultado).isEmpty();
        verify(jdbcTemplate).query(contains("solicitante = ?"), reservaMapper(), eq("Pedro"));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveSalvarReservaSemParticipantes() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);
        Reserva reserva = reservaSemParticipantes();

        Reserva salva = repository.salvar(reserva);

        assertThat(salva).isSameAs(reserva);
        verify(jdbcTemplate).update(
                contains("INSERT INTO reserva"),
                eq(reserva.getId().toString()),
                eq(reserva.getSalaId().toString()),
                eq("Pedro"),
                eq(INICIO.toString()),
                eq(FIM.toString()),
                eq(StatusReserva.CONFIRMADA.name()));
        verify(jdbcTemplate).update(contains("DELETE FROM reserva_participante"), eq(reserva.getId().toString()));
        verify(jdbcTemplate, never()).update(contains("INSERT INTO reserva_participante"), any(Object[].class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveSalvarReservaComParticipantes() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);
        Reserva reserva = reservaSemParticipantes();
        reserva.adicionarParticipante(new Participante("Maria"));
        reserva.adicionarParticipante(new Participante("Joao"));

        repository.salvar(reserva);

        verify(jdbcTemplate).update(
                contains("INSERT INTO reserva_participante"),
                eq(reserva.getId().toString()),
                eq("Maria"));
        verify(jdbcTemplate).update(
                contains("INSERT INTO reserva_participante"),
                eq(reserva.getId().toString()),
                eq("Joao"));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarVazioQuandoSalaNaoExiste() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), salaMapper(), eq(salaId.toString()))).thenReturn(List.of());
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        Optional<Sala> resultado = repository.buscarPorId(salaId);

        assertThat(resultado).isEmpty();
        verify(jdbcTemplate).query(contains("WHERE id = ?"), salaMapper(), eq(salaId.toString()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveBuscarSalaPorId() {
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), salaMapper(), eq(salaId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<Sala> mapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.getString("id")).thenReturn(salaId.toString());
                    when(rs.getString("nome")).thenReturn("Sala 01");
                    when(rs.getInt("capacidade")).thenReturn(10);
                    return List.of(mapper.mapRow(rs, 0));
                });
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        Optional<Sala> resultado = repository.buscarPorId(salaId);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(salaId);
        assertThat(resultado.get().getNome()).isEqualTo("Sala 01");
        assertThat(resultado.get().getCapacidade()).isEqualTo(10);
        verify(jdbcTemplate).query(contains("WHERE id = ?"), salaMapper(), eq(salaId.toString()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveSalvarSala() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);

        Sala salva = repository.salvar(sala);

        assertThat(salva).isSameAs(sala);
        verify(jdbcTemplate).update(
                contains("INSERT INTO sala"),
                eq(sala.getId().toString()),
                eq("Sala 01"),
                eq(10));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRemoverSala() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);
        UUID salaId = UUID.randomUUID();

        repository.remover(salaId);

        verify(jdbcTemplate).update(contains("DELETE FROM sala"), eq(salaId.toString()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveConsiderarNomeExistenteQuandoQuantidadeForPositiva() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        assertThat(repository.existePorNome("Sala 01")).isTrue();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveConsiderarNomeInexistenteQuandoQuantidadeForZero() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(0);
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        assertThat(repository.existePorNome("Sala 01")).isFalse();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveConsiderarNomeInexistenteQuandoQuantidadeForNula() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(null);
        JdbcSalaRepository repository = new JdbcSalaRepository(jdbcTemplate);

        assertThat(repository.existePorNome("Sala 01")).isFalse();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarFalsoNoMetodoPadraoDeExistePorNome() {
        SalaRepository repository = new SalaRepository() {
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

        assertThat(repository.existePorNome("Sala 01")).isFalse();
    }

    private static RowMapper<Reserva> reservaMapper() {
        return any();
    }

    private static RowMapper<Sala> salaMapper() {
        return any();
    }

    private static RowMapper<Object> qualquerMapper() {
        return any();
    }

    private JdbcTemplate jdbcQueMapeiaReserva(UUID reservaId, UUID salaId, boolean comParticipante) {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), qualquerMapper(), any(Object[].class))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            RowMapper<Object> mapper = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            if (sql.contains("reserva_participante")) {
                if (!comParticipante) {
                    return List.of();
                }
                when(rs.getString("nome")).thenReturn("Maria");
                return List.of(mapper.mapRow(rs, 0));
            }
            when(rs.getString("id")).thenReturn(reservaId.toString());
            when(rs.getString("sala_id")).thenReturn(salaId.toString());
            when(rs.getString("solicitante")).thenReturn("Pedro");
            when(rs.getString("inicio")).thenReturn(INICIO.toString());
            when(rs.getString("fim")).thenReturn(FIM.toString());
            when(rs.getString("status")).thenReturn(StatusReserva.CONFIRMADA.name());
            return List.of(mapper.mapRow(rs, 0));
        });
        return jdbcTemplate;
    }

    private Reserva reservaSemParticipantes() {
        return new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pedro",
                new PeriodoReserva(INICIO, FIM),
                StatusReserva.CONFIRMADA);
    }
}
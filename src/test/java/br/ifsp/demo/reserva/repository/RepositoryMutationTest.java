package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryMutationTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 6, 9, 0);
    private static final LocalDateTime FIM = INICIO.plusHours(1);

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveRetornarReservasEncontradasAoBuscarComFiltros() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = jdbcQueMapeiaReserva(reservaId, salaId);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarComFiltros(salaId, INICIO, FIM, "Pedro");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(reservaId);
        assertThat(resultado.get(0).getSalaId()).isEqualTo(salaId);
        assertThat(resultado.get(0).getSolicitante()).isEqualTo("Pedro");
        assertThat(resultado.get(0).getParticipantes())
                .extracting(Participante::getNome)
                .containsExactly("Maria");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    void deveRetornarReservasEncontradasAoBuscarPorSolicitante() {
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        JdbcTemplate jdbcTemplate = jdbcQueMapeiaReserva(reservaId, salaId);
        JdbcReservaRepository repository = new JdbcReservaRepository(jdbcTemplate);

        List<Reserva> resultado = repository.buscarPorSolicitante("Pedro");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(reservaId);
        assertThat(resultado.get(0).getSalaId()).isEqualTo(salaId);
        assertThat(resultado.get(0).getSolicitante()).isEqualTo("Pedro");
        assertThat(resultado.get(0).getParticipantes())
                .extracting(Participante::getNome)
                .containsExactly("Maria");
        verify(jdbcTemplate).query(contains("solicitante = ?"), reservaMapper(), eq("Pedro"));
    }

    private static RowMapper<Reserva> reservaMapper() {
        return any();
    }

    private static RowMapper<Object> qualquerMapper() {
        return any();
    }

    private JdbcTemplate jdbcQueMapeiaReserva(UUID reservaId, UUID salaId) {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), qualquerMapper(), any(Object[].class))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            RowMapper<Object> mapper = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            if (sql.contains("reserva_participante")) {
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
}

package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcReservaRepository implements ReservaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Reserva> ROW_MAPPER = (rs, rowNum) -> new Reserva(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("sala_id")),
            rs.getString("solicitante"),
            new PeriodoReserva(
                    LocalDateTime.parse(rs.getString("inicio")),
                    LocalDateTime.parse(rs.getString("fim"))
            ),
            StatusReserva.valueOf(rs.getString("status"))
    );

    @Override
    public java.util.Optional<Reserva> buscarPorId(UUID reservaId) {
        String sql = "SELECT * FROM reserva WHERE id = ?";
        List<Reserva> reservas = jdbcTemplate.query(sql, ROW_MAPPER, reservaId.toString());
        return reservas.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(reservas.getFirst());
    }

    @Override
    public List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT * FROM reserva WHERE sala_id = ? AND status = 'CONFIRMADA' AND inicio < ? AND fim > ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, salaId.toString(), fim.toString(), inicio.toString());
    }

    @Override
    public Reserva salvar(Reserva reserva) {
        String sql = "INSERT INTO reserva (id, sala_id, solicitante, inicio, fim, status) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET sala_id = excluded.sala_id, solicitante = excluded.solicitante, inicio = excluded.inicio, fim = excluded.fim, status = excluded.status";
        jdbcTemplate.update(sql,
                reserva.getId().toString(),
                reserva.getSalaId().toString(),
                reserva.getSolicitante(),
                reserva.getPeriodo().getInicio().toString(),
                reserva.getPeriodo().getFim().toString(),
                reserva.getStatus().name());
        return reserva;
    }
}

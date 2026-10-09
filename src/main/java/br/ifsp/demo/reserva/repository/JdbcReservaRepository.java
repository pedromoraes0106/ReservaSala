package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.StatusReserva;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

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
        return reservas.isEmpty()
            ? java.util.Optional.empty()
            : java.util.Optional.of(carregarParticipantes(reservas.getFirst()));
    }

    @Override
    public List<Reserva> buscarPorSalaEPeriodo(UUID salaId, LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT * FROM reserva WHERE sala_id = ? AND status = 'CONFIRMADA' AND inicio < ? AND fim > ?";
        return carregarParticipantes(jdbcTemplate.query(sql, ROW_MAPPER, salaId.toString(), fim.toString(), inicio.toString()));
    }

    @Override
    public List<Reserva> buscarComFiltros(UUID salaId, LocalDateTime inicio, LocalDateTime fim, String solicitante) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reserva WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (salaId != null) {
            sql.append(" AND sala_id = ?");
            parametros.add(salaId.toString());
        }

        if (solicitante != null && !solicitante.isBlank()) {
            sql.append(" AND solicitante = ?");
            parametros.add(solicitante);
        }

        if (inicio != null && fim != null) {
            sql.append(" AND inicio < ? AND fim > ?");
            parametros.add(fim.toString());
            parametros.add(inicio.toString());
        }

        return carregarParticipantes(jdbcTemplate.query(sql.toString(), ROW_MAPPER, parametros.toArray()));
    }

    @Override
    public List<Reserva> buscarFuturasConfirmadasPorSala(UUID salaId, LocalDateTime aPartirDe) {
        String sql = "SELECT * FROM reserva WHERE sala_id = ? AND status = 'CONFIRMADA' AND inicio > ?";
        return carregarParticipantes(jdbcTemplate.query(sql, ROW_MAPPER, salaId.toString(), aPartirDe.toString()));
    }

    @Override
    @Transactional
    public List<Reserva> buscarPorSolicitante(String solicitante) {
        String sql = "SELECT * FROM reserva WHERE solicitante = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, solicitante);
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
        jdbcTemplate.update("DELETE FROM reserva_participante WHERE reserva_id = ?", reserva.getId().toString());
        for (Participante participante : reserva.getParticipantes()) {
            jdbcTemplate.update(
                    "INSERT INTO reserva_participante (reserva_id, nome) VALUES (?, ?)",
                    reserva.getId().toString(),
                    participante.getNome()
            );
        }
        return reserva;
    }

    private List<Reserva> carregarParticipantes(List<Reserva> reservas) {
        reservas.forEach(this::carregarParticipantes);
        return reservas;
    }

    private Reserva carregarParticipantes(Reserva reserva) {
        List<Participante> participantes = jdbcTemplate.query(
                "SELECT nome FROM reserva_participante WHERE reserva_id = ?",
                (rs, rowNum) -> new Participante(rs.getString("nome")),
                reserva.getId().toString()
        );
        participantes.forEach(reserva::adicionarParticipante);
        return reserva;
    }
}

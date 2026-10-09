package br.ifsp.demo.reserva.repository;

import br.ifsp.demo.reserva.domain.Sala;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSalaRepository implements SalaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSalaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Sala> ROW_MAPPER = (rs, rowNum) -> new Sala(
            UUID.fromString(rs.getString("id")),
            rs.getString("nome"),
            rs.getInt("capacidade")
    );

    @Override
    public Optional<Sala> buscarPorId(UUID id) {
        String sql = "SELECT * FROM sala WHERE id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, id.toString()).stream().findFirst();
    }

    @Override
    public Sala salvar(Sala sala) {
        String sql = "INSERT INTO sala (id, nome, capacidade) VALUES (?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET nome = excluded.nome, capacidade = excluded.capacidade";
        jdbcTemplate.update(sql, sala.getId().toString(), sala.getNome(), sala.getCapacidade());
        return sala;
    }

    @Override
    public void remover(UUID id) {
        String sql = "DELETE FROM sala WHERE id = ?";
        jdbcTemplate.update(sql, id.toString());
    }
}

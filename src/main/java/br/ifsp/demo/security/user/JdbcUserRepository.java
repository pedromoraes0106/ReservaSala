package br.ifsp.demo.security.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements JpaUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setName(rs.getString("name"));
        user.setLastname(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(Role.valueOf(rs.getString("role")));
        return user;
    };

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM app_user WHERE email = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, email).stream().findFirst();
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO app_user (id, name, lastname, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getId().toString(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name());
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT * FROM app_user WHERE id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, id.toString()).stream().findFirst();
    }
}

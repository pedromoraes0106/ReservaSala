package br.ifsp.demo.security.user;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository {
    Optional<User> findByEmail(String email);
    void save(User user);
    Optional<User> findById(UUID id);
}

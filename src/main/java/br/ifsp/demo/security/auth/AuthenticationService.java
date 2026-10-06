package br.ifsp.demo.security.auth;

import br.ifsp.demo.exception.EntityAlreadyExistsException;
import br.ifsp.demo.security.config.JwtService;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.Role;
import br.ifsp.demo.security.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JpaUserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(unused -> {
            throw new EntityAlreadyExistsException("Email already registered: " + request.email());
        });

        String encryptedPassword = passwordEncoder.encode(request.password());

        UUID id = UUID.randomUUID();
        User user = new User(id, request.name(), request.lastname(), request.email(), encryptedPassword, Role.USER);

        userRepository.save(user);
        return new RegisterUserResponse(id);
    }

    public AuthResponse authenticate(AuthRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmail(request.username()).orElseThrow();
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}

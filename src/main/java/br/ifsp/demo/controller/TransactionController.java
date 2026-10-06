package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/hello")
@Tag(name = "Hello API")
public class TransactionController {

    private final AuthenticationInfoService authService;

    public TransactionController(AuthenticationInfoService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<String> hello() {
        UUID userId = authService.getAuthenticatedUserId();
        return ResponseEntity.ok("Hello: " + userId);
    }
}

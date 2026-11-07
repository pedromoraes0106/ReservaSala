package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.AuthenticationInfoService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionControllerStructuralTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarOkComIdDoUsuarioAutenticado() {
        UUID userId = UUID.randomUUID();
        AuthenticationInfoService authService = new AuthenticationInfoService() {
            @Override
            public UUID getAuthenticatedUserId() {
                return userId;
            }
        };
        TransactionController controller = new TransactionController(authService);

        ResponseEntity<String> resposta = controller.hello();

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).isEqualTo("Hello: " + userId);
    }
}
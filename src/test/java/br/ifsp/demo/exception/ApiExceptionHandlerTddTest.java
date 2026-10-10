package br.ifsp.demo.exception;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiExceptionHandlerTddTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveMapearReservaCanceladaParaConflito() {
        ResponseEntity<?> response = handler.handleReservaCanceladaException(
                new ReservaCanceladaException("reserva já está cancelada.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveMapearReservaInexistenteParaNaoEncontrado() {
        ResponseEntity<?> response = handler.handleReservaNaoEncontradaException(
                new ReservaNaoEncontradaException(UUID.randomUUID())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    void deveMapearParticipanteInexistenteParaNaoEncontrado() {
        ResponseEntity<?> response = handler.handleParticipanteNaoEncontradoException(
                new ParticipanteNaoEncontradoException(new Participante("Maria"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
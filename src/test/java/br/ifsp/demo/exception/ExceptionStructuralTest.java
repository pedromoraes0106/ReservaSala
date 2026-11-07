package br.ifsp.demo.exception;

import br.ifsp.demo.reserva.domain.Participante;
import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.ParticipanteNaoEncontradoException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionStructuralTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveExporOsValoresInformadosNoConstrutorDeApiException() {
        ZonedDateTime timestamp = ZonedDateTime.now();

        ApiException apiException = new ApiException("Mensagem", HttpStatus.CONFLICT, timestamp, "dev.Message");

        assertThat(apiException.getMessage()).isEqualTo("Mensagem");
        assertThat(apiException.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getTimestamp()).isEqualTo(timestamp);
        assertThat(apiException.getDeveloperMessage()).isEqualTo("dev.Message");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCriarApiExceptionPeloMetodoOfUsandoONomeDaClasseDaExcecao() {
        ZonedDateTime antes = ZonedDateTime.now();

        ApiException apiException = ApiException.of("Nao encontrado", HttpStatus.NOT_FOUND, new IllegalStateException("x"));

        ZonedDateTime depois = ZonedDateTime.now();
        assertThat(apiException.getMessage()).isEqualTo("Nao encontrado");
        assertThat(apiException.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getDeveloperMessage()).isEqualTo(IllegalStateException.class.getName());
        assertThat(apiException.getTimestamp()).isBetween(antes, depois);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveGuardarMensagemEmEntityNotFoundException() {
        EntityNotFoundException excecao = new EntityNotFoundException("Entidade nao encontrada");

        assertThat(excecao).isInstanceOf(RuntimeException.class);
        assertThat(excecao.getMessage()).isEqualTo("Entidade nao encontrada");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveGuardarMensagemEmEntityAlreadyExistsException() {
        EntityAlreadyExistsException excecao = new EntityAlreadyExistsException("Entidade ja existe");

        assertThat(excecao).isInstanceOf(RuntimeException.class);
        assertThat(excecao.getMessage()).isEqualTo("Entidade ja existe");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarNullPointerExceptionComBadRequest() {
        NullPointerException excecao = new NullPointerException("valor nulo");

        ResponseEntity<?> resposta = handler.handleNullPointerException(excecao);

        verificarResposta(resposta, HttpStatus.BAD_REQUEST, "valor nulo", NullPointerException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarConflitoDeHorarioExceptionComConflict() {
        ConflitoDeHorarioException excecao = new ConflitoDeHorarioException("horario ocupado");

        ResponseEntity<?> resposta = handler.handleConflitoDeHorarioException(excecao);

        verificarResposta(resposta, HttpStatus.CONFLICT, "horario ocupado", ConflitoDeHorarioException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarSalaNaoEncontradaExceptionComNotFound() {
        SalaNaoEncontradaException excecao = new SalaNaoEncontradaException("sala inexistente");

        ResponseEntity<?> resposta = handler.handleSalaNaoEncontradaException(excecao);

        verificarResposta(resposta, HttpStatus.NOT_FOUND, "sala inexistente", SalaNaoEncontradaException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarReservaNaoEncontradaExceptionComNotFound() {
        ReservaNaoEncontradaException excecao = new ReservaNaoEncontradaException(UUID.randomUUID());

        ResponseEntity<?> resposta = handler.handleReservaNaoEncontradaException(excecao);

        verificarResposta(resposta, HttpStatus.NOT_FOUND, excecao.getMessage(), ReservaNaoEncontradaException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarParticipanteNaoEncontradoExceptionComNotFound() {
        ParticipanteNaoEncontradoException excecao = new ParticipanteNaoEncontradoException(new Participante("Maria"));

        ResponseEntity<?> resposta = handler.handleParticipanteNaoEncontradoException(excecao);

        verificarResposta(resposta, HttpStatus.NOT_FOUND, excecao.getMessage(), ParticipanteNaoEncontradoException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarReservaCanceladaExceptionComConflict() {
        ReservaCanceladaException excecao = new ReservaCanceladaException("reserva cancelada");

        ResponseEntity<?> resposta = handler.handleReservaCanceladaException(excecao);

        verificarResposta(resposta, HttpStatus.CONFLICT, "reserva cancelada", ReservaCanceladaException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarIllegalArgumentExceptionComBadRequest() {
        IllegalArgumentException excecao = new IllegalArgumentException("argumento invalido");

        ResponseEntity<?> resposta = handler.handleIllegalArgumentException(excecao);

        verificarResposta(resposta, HttpStatus.BAD_REQUEST, "argumento invalido", IllegalArgumentException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarIllegalStateExceptionComForbidden() {
        IllegalStateException excecao = new IllegalStateException("estado invalido");

        ResponseEntity<?> resposta = handler.handleIllegalStateException(excecao);

        verificarResposta(resposta, HttpStatus.FORBIDDEN, "estado invalido", IllegalStateException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarEntityNotFoundExceptionComNotFound() {
        EntityNotFoundException excecao = new EntityNotFoundException("entidade nao encontrada");

        ResponseEntity<?> resposta = handler.handleEntityNotFoundException(excecao);

        verificarResposta(resposta, HttpStatus.NOT_FOUND, "entidade nao encontrada", EntityNotFoundException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveTratarEntityAlreadyExistsExceptionComConflict() {
        EntityAlreadyExistsException excecao = new EntityAlreadyExistsException("entidade ja existe");

        ResponseEntity<?> resposta = handler.handleEntityAlreadyExistsException(excecao);

        verificarResposta(resposta, HttpStatus.CONFLICT, "entidade ja existe", EntityAlreadyExistsException.class);
    }

    private void verificarResposta(
            ResponseEntity<?> resposta,
            HttpStatus statusEsperado,
            String mensagemEsperada,
            Class<? extends Throwable> tipoDaExcecao) {
        assertThat(resposta.getStatusCode()).isEqualTo(statusEsperado);
        assertThat(resposta.getBody()).isInstanceOf(ApiException.class);

        ApiException corpo = (ApiException) resposta.getBody();
        assertThat(corpo.getMessage()).isEqualTo(mensagemEsperada);
        assertThat(corpo.getStatus()).isEqualTo(statusEsperado);
        assertThat(corpo.getDeveloperMessage()).isEqualTo(tipoDaExcecao.getName());
        assertThat(corpo.getTimestamp()).isNotNull();
    }
}

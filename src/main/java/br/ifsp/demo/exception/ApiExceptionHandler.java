package br.ifsp.demo.exception;

import br.ifsp.demo.reserva.exception.ConflitoDeHorarioException;
import br.ifsp.demo.reserva.exception.ReservaCanceladaException;
import br.ifsp.demo.reserva.exception.ReservaNaoEncontradaException;
import br.ifsp.demo.reserva.exception.SalaNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        HttpStatus badRequest = BAD_REQUEST;
        ApiException apiException = new ApiException(e.getMessage(), badRequest, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = ConflitoDeHorarioException.class)
    public ResponseEntity<?> handleConflitoDeHorarioException(ConflitoDeHorarioException e) {
        ApiException apiException = new ApiException(e.getMessage(), CONFLICT, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, CONFLICT);
    }

    @ExceptionHandler(value = SalaNaoEncontradaException.class)
    public ResponseEntity<?> handleSalaNaoEncontradaException(SalaNaoEncontradaException e) {
        ApiException apiException = new ApiException(e.getMessage(), NOT_FOUND, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, NOT_FOUND);
    }

    @ExceptionHandler(value = ReservaNaoEncontradaException.class)
    public ResponseEntity<?> handleReservaNaoEncontradaException(ReservaNaoEncontradaException e) {
        ApiException apiException = new ApiException(e.getMessage(), NOT_FOUND, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, NOT_FOUND);
    }

    @ExceptionHandler(value = ReservaCanceladaException.class)
    public ResponseEntity<?> handleReservaCanceladaException(ReservaCanceladaException e) {
        ApiException apiException = new ApiException(e.getMessage(), CONFLICT, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, CONFLICT);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus badRequest = BAD_REQUEST;
        ApiException apiException = new ApiException(e.getMessage(), badRequest, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        HttpStatus forbidden = FORBIDDEN;
        ApiException apiException = new ApiException(e.getMessage(), forbidden, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, forbidden);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus notFound = NOT_FOUND;
        ApiException apiException = new ApiException(e.getMessage(), notFound, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, notFound);
    }

    @ExceptionHandler(value = EntityAlreadyExistsException.class)
    public ResponseEntity<?> handleEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        HttpStatus conflict = CONFLICT;
        ApiException apiException = new ApiException(e.getMessage(), conflict, ZonedDateTime.now(), e.getClass().getName());
        return new ResponseEntity<>(apiException, conflict);
    }
}

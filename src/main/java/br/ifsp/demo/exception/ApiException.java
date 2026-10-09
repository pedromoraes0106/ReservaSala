package br.ifsp.demo.exception;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ApiException {
    private final String message;
    private final HttpStatus status;
    private final ZonedDateTime timestamp;
    private final String developerMessage;

    public ApiException(String message, HttpStatus status, ZonedDateTime timestamp, String developerMessage) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.developerMessage = developerMessage;
    }

    public static ApiException of(String message, HttpStatus status, Throwable throwable) {
        return new ApiException(message, status, ZonedDateTime.now(), throwable.getClass().getName());
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }
}
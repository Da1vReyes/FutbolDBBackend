package edu.uam.futboldb.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe recibir el cliente. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException conflicto(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException noAutorizado(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException prohibido(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}

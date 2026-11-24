package com.induztek.activos.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos.", request, fields);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> constraint(ConstraintViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<?> unreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "El cuerpo JSON no tiene el formato esperado.", request, Map.of());
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException ex, HttpServletRequest request) {
        return response(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), request, Map.of());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> argument(IllegalArgumentException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> forbidden(AccessDeniedException ex, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta operación.", request, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "El registro entra en conflicto con datos existentes.", request, Map.of());
    }
    @ExceptionHandler(WebClientResponseException.class)
    ResponseEntity<?> upstream(WebClientResponseException ex, HttpServletRequest request) {
        log.warn("Fallo de servicio aguas arriba (Usuarios): HTTP {} en {}", ex.getStatusCode().value(), request.getRequestURI(), ex);
        HttpStatus status = ex.getStatusCode().value() == 404
            ? HttpStatus.NOT_FOUND
            : ex.getStatusCode().is4xxClientError() ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
        return response(status, "No fue posible completar la consulta con el microservicio de Usuarios.", request, Map.of());
    }
    @ExceptionHandler(WebClientRequestException.class)
    ResponseEntity<?> upstreamUnavailable(WebClientRequestException ex, HttpServletRequest request) {
        log.warn("No se pudo conectar con Usuarios al procesar {}", request.getRequestURI(), ex);
        return response(HttpStatus.SERVICE_UNAVAILABLE, "El microservicio de Usuarios no está disponible.", request, Map.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Error interno al procesar {}", request.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno.", request, Map.of());
    }
    private ResponseEntity<?> response(HttpStatus status, String message, HttpServletRequest request, Map<?, ?> details) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now(), "status", status.value(),
            "error", status.getReasonPhrase(), "message", message == null ? "Error" : message,
            "path", request.getRequestURI(), "details", details));
    }
}

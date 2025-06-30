package com.popoworld.backend.global.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", java.time.LocalDateTime.now());
        return body;
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            RuntimeException.class,
            EntityNotFoundException.class,
            AccessDeniedException.class,
            IllegalStateException.class,
    })
    public ResponseEntity<Map<String, Object>> handleCommonExceptions(RuntimeException ex) {
        HttpStatus status;

        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(status)
                .body(createErrorResponse(ex.getMessage(), status));
    }
}

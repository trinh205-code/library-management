package com.slayer.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleEnumError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "error", "Invalid enum value",
                        "message", ex.getMessage()
                )
        );
    }
}
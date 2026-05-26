package com.example.teodormihai.demo.web;

import com.example.teodormihai.demo.service.DuplicateNameException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, DuplicateNameException.class})
    public ResponseEntity<Map<String, String>> handleBadInput(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(IllegalStateException ex) {
        return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }
}

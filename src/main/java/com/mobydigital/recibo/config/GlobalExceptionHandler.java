package com.mobydigital.recibo.config;

import com.mobydigital.recibo.exception.FirmaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FirmaException.class)
    public ResponseEntity<Map<String, Object>> manejarFirmaException(FirmaException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Error en el proceso de firma");
        body.put("mensaje", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErroresGenericos(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Error interno del servidor");
        body.put("mensaje", "Ocurrió un problema inesperado: " + ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

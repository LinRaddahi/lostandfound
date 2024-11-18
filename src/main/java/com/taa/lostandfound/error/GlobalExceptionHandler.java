package com.taa.lostandfound.error;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach((fieldError) ->
            errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(NotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(InvalidPassException.class)
    public ResponseEntity<Object> handleInvalidPassException(InvalidPassException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }

    @ExceptionHandler(PDFExtractionException.class)
    public ResponseEntity<Object> handlePDFExtractionException(PDFExtractionException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<Object> handleMissingHeaderException(MissingHeaderException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<Object> handleJwtValidationException(JwtValidationException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(JwtException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }

    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<Object> handleParsingException(ParsingException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }
}

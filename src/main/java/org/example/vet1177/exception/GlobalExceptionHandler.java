package org.example.vet1177.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                              errors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                                      .add(error.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);

        return new ErrorResponse(
                400,
                "Validation failed",
                errors
        );
    }

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return new ErrorResponse(404, ex.getMessage(), null);
    }

    // Access denied
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return new ErrorResponse(403, ex.getMessage(), null);
    }

    // Business rule violation
    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessRule(BusinessRuleException ex) {

        log.warn("Business rule violation: {}", ex.getMessage());

        return new ErrorResponse(400, ex.getMessage(), null);
    }

    // Fallback (ALLA andra errors)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return new ErrorResponse(500, "Something went wrong", null);
    }
}
package org.example.vet1177.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String message;
    private Map<String, List<String>> errors;

    public ErrorResponse(int status, String message, Map<String, List<String>> errors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, List<String>> getErrors() { return errors; }
}
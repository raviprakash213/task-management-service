package com.epam.AsyncDataPipeline.dto;

import com.epam.AsyncDataPipeline.enums.ErrorType;
import java.time.Instant;

/**
 * Data Transfer Object (DTO) for representing error responses in the application.
 * This class encapsulates error details such as the error message, type, and timestamp.
 * It is primarily used for providing consistent error responses in API exception handling.
 */
public class ErrorResponse {
    private final String message;
    private final ErrorType errorType;
    private final Instant timestamp;

    public ErrorResponse(String message, ErrorType errorType) {
        this.message = message;
        this.errorType = errorType;
        this.timestamp = Instant.now(); // Always UTC
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

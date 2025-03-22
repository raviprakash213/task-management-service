package com.epam.AsyncDataPipeline.exceptionHandler;

import com.epam.AsyncDataPipeline.dto.ErrorResponse;
import com.epam.AsyncDataPipeline.enums.ErrorType;
import com.epam.AsyncDataPipeline.exception.InvalidSortDirectionException;
import com.epam.AsyncDataPipeline.exception.TaskNotFoundException;
import com.epam.AsyncDataPipeline.exception.TaskProcessingException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Global exception handler for handling application-wide exceptions.
 * This class intercepts exceptions thrown by controllers and provides a standardized
 * response format using  ErrorResponse It ensures proper HTTP status codes
 * and error messages are returned for different types of errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, ErrorType errorType, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), errorType);
        return ResponseEntity.status(status).body(errorResponse);
    }


    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        logger.error("Task not found: {}", ex.getMessage());
        return buildErrorResponse(ex, ErrorType.DATA_ERROR, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskProcessingException.class)
    public ResponseEntity<ErrorResponse> handleTaskProcessingException(TaskProcessingException ex) {
        logger.error("Task processing error: {}", ex.getMessage());
        return buildErrorResponse(ex, ErrorType.SYSTEM_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RequestNotPermitted ex) {
        logger.error("Rate limit exceeded: {}", ex.getMessage());
        return buildErrorResponse(ex, ErrorType.SYSTEM_ERROR, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException ex) {
        logger.error("Invalid property reference: {}", ex.getMessage());
        return buildErrorResponse(ex, ErrorType.DATA_ERROR, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSortDirectionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortDirectionException(InvalidSortDirectionException ex) {
        logger.error("Invalid sorting direction: {}", ex.getMessage());
        return buildErrorResponse(ex, ErrorType.DATA_ERROR, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", errors);
        logger.error("Validation failed: {}", errorMessage);

        return buildErrorResponse(new Exception(errorMessage), ErrorType.DATA_ERROR, HttpStatus.BAD_REQUEST);
    }

}
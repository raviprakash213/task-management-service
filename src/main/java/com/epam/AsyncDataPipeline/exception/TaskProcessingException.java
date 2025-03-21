package com.epam.AsyncDataPipeline.exception;

/**
 * Exception thrown when an error occurs during task processing.
 * This exception is used to indicate failures in processing tasks,
 * such as unexpected errors or system issues.
 */
public class TaskProcessingException extends RuntimeException {
    public TaskProcessingException(String message) {
        super(message);
    }
}
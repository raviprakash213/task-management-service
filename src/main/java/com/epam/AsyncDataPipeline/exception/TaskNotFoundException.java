package com.epam.AsyncDataPipeline.exception;

/**
 * Exception thrown when a requested task is not found.
 * This exception is used to indicate that a task with the specified ID
 * does not exist in the system.
 */
public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(String message) {
        super(message);
    }
}

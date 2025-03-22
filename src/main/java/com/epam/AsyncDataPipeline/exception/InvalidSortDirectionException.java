package com.epam.AsyncDataPipeline.exception;

public class InvalidSortDirectionException extends RuntimeException {
    public InvalidSortDirectionException(String direction) {
        super("Invalid sorting direction: '" + direction + "'. Allowed values: 'asc' or 'desc'.");
    }
}

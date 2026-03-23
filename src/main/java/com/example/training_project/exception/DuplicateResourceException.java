package com.example.training_project.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(final String message) {
        super(message);
    }
}

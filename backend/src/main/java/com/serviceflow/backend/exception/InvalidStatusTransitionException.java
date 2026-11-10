package com.serviceflow.backend.exception;

/**
 * Exception thrown when an invalid booking status transition is attempted.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

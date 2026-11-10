package com.serviceflow.backend.exception;

/**
 * Exception thrown when a job assignment conflicts with existing assignments.
 * Used for scheduling conflicts (technician/bay already assigned during the same time period).
 */
public class AssignmentConflictException extends RuntimeException {

    public AssignmentConflictException(String message) {
        super(message);
    }
}

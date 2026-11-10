package com.serviceflow.backend.entity;

/**
 * Enum representing the status of a job assignment.
 * Assignments track technician and bay allocation throughout the service lifecycle.
 */
public enum AssignmentStatus {
    ACTIVE,      // Currently assigned and in progress
    COMPLETED,   // Service work completed
    CANCELLED    // Assignment cancelled or superseded
}

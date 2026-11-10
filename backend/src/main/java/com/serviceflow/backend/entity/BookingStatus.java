package com.serviceflow.backend.entity;

/**
 * Booking status enum representing the lifecycle of a service booking.
 * 
 * Valid transitions:
 * REQUESTED → CONFIRMED
 * REQUESTED → CANCELLED
 * CONFIRMED → CANCELLED
 * CONFIRMED → CHECKED_IN
 * 
 * CANCELLED and CHECKED_IN are terminal states.
 */
public enum BookingStatus {
    /**
     * Initial state when a booking is first created.
     */
    REQUESTED,
    
    /**
     * Booking has been confirmed by the service center.
     */
    CONFIRMED,
    
    /**
     * Booking has been cancelled. Terminal state.
     */
    CANCELLED,
    
    /**
     * Customer has checked in. Ready for service job creation. Terminal state.
     */
    CHECKED_IN
}

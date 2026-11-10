package com.serviceflow.backend.entity;

/**
 * Job status enum representing the lifecycle of a service job card.
 * 
 * Complete workflow from booking check-in to delivery.
 * 
 * Valid transitions:
 * BOOKED → CHECKED_IN
 * CHECKED_IN → INSPECTION
 * INSPECTION → ESTIMATE_PENDING
 * ESTIMATE_PENDING → AWAITING_APPROVAL
 * AWAITING_APPROVAL → APPROVED
 * APPROVED → IN_SERVICE
 * IN_SERVICE → QUALITY_CHECK
 * QUALITY_CHECK → READY_FOR_DELIVERY
 * QUALITY_CHECK → IN_SERVICE (rework after failed inspection)
 * READY_FOR_DELIVERY → DELIVERED
 */
public enum JobStatus {
    /**
     * Job created from booking. Initial state.
     */
    BOOKED,
    
    /**
     * Customer has checked in at the service center.
     */
    CHECKED_IN,
    
    /**
     * Vehicle inspection in progress.
     */
    INSPECTION,
    
    /**
     * Inspection complete, estimate preparation pending.
     */
    ESTIMATE_PENDING,
    
    /**
     * Estimate prepared, awaiting customer approval.
     */
    AWAITING_APPROVAL,
    
    /**
     * Customer approved the estimate.
     */
    APPROVED,
    
    /**
     * Service work in progress.
     */
    IN_SERVICE,
    
    /**
     * Quality check in progress.
     */
    QUALITY_CHECK,
    
    /**
     * Quality check passed, ready for customer delivery.
     */
    READY_FOR_DELIVERY,
    
    /**
     * Vehicle delivered to customer. Terminal state.
     */
    DELIVERED
}

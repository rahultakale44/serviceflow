package com.serviceflow.backend.entity;

/**
 * EstimateStatus enum representing the lifecycle of a repair estimate.
 * 
 * Valid transitions:
 * DRAFT → PENDING_APPROVAL (estimate submitted for customer approval)
 * PENDING_APPROVAL → APPROVED (customer approves estimate)
 * PENDING_APPROVAL → REJECTED (customer rejects estimate)
 * PENDING_APPROVAL → SUPERSEDED (new revision created, old estimate becomes superseded)
 * 
 * Terminal states: APPROVED, REJECTED, SUPERSEDED
 */
public enum EstimateStatus {
    /**
     * Estimate is being prepared. Can be edited before submission.
     */
    DRAFT,
    
    /**
     * Estimate submitted to customer, awaiting approval/rejection.
     */
    PENDING_APPROVAL,
    
    /**
     * Customer approved the estimate. Terminal state.
     */
    APPROVED,
    
    /**
     * Customer rejected the estimate. Terminal state.
     */
    REJECTED,
    
    /**
     * Estimate superseded by a newer revision. Terminal state.
     */
    SUPERSEDED
}

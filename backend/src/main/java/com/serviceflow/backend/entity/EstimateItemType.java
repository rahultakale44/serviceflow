package com.serviceflow.backend.entity;

/**
 * EstimateItemType enum representing the type of line item in a repair estimate.
 */
public enum EstimateItemType {
    /**
     * Labour cost for service work.
     */
    LABOUR,
    
    /**
     * Parts cost for replacement or repair.
     */
    PART
}

package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.EstimateStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RepairEstimateResponseDto(
        Long id,
        String estimateNumber,
        Long jobId,
        String jobCardNumber,
        Integer version,
        EstimateStatus estimateStatus,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        List<RepairEstimateItemResponseDto> items
) {
}

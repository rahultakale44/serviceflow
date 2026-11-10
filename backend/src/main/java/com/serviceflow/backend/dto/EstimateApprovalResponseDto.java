package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.ApprovalDecision;

import java.time.LocalDateTime;

public record EstimateApprovalResponseDto(
        Long id,
        Long estimateId,
        String estimateNumber,
        Long customerId,
        String customerName,
        ApprovalDecision decision,
        LocalDateTime decisionTimestamp,
        String rejectionReason,
        String customerComments
) {
}

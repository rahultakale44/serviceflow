package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.InspectionCategory;
import com.serviceflow.backend.entity.InspectionCondition;

import java.time.LocalDateTime;

public record JobInspectionResponseDto(
        Long id,
        Long jobId,
        InspectionCategory category,
        String itemName,
        InspectionCondition condition,
        String notes,
        LocalDateTime inspectedAt
) {
}

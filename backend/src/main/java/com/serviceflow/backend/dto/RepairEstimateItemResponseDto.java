package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.EstimateItemType;

import java.math.BigDecimal;

public record RepairEstimateItemResponseDto(
        Long id,
        String description,
        EstimateItemType itemType,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}

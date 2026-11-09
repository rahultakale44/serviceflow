package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for returning service item data.
 * Includes all service information for client consumption.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItemResponseDto {

    private Long id;
    private String serviceCode;
    private String name;
    private String description;
    private String category;
    private BigDecimal basePrice;
    private Integer estimatedDurationMinutes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

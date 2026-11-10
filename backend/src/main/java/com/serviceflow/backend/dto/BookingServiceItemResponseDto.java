package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for booking service item (line item snapshot).
 * Contains historical service details preserved at booking time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingServiceItemResponseDto {

    private Long id;
    private Long serviceItemId;
    private String serviceCode;
    private String serviceName;
    private BigDecimal unitPrice;
    private Integer estimatedDurationMinutes;
    private Integer quantity;
    private BigDecimal lineTotal;
}

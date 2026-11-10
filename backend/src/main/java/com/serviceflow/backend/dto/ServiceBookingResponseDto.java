package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for service booking.
 * Contains all booking details including customer/vehicle summaries
 * and service line items with historical snapshots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBookingResponseDto {

    private Long id;
    private String bookingNumber;
    private CustomerSummaryDto customer;
    private VehicleSummaryDto vehicle;
    private LocalDateTime bookingDateTime;
    private String customerNotes;
    private BookingStatus bookingStatus;
    private BigDecimal estimatedTotalPrice;
    private Integer estimatedTotalDurationMinutes;
    private List<BookingServiceItemResponseDto> services;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

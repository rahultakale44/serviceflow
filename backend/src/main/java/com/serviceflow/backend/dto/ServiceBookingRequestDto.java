package com.serviceflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a service booking.
 * Client provides customer, vehicle, booking time, and selected services.
 * Prices, totals, and booking number are calculated server-side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBookingRequestDto {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Booking date/time is required")
    @Future(message = "Booking date/time must be in the future")
    private LocalDateTime bookingDateTime;

    private String customerNotes;

    @NotEmpty(message = "At least one service must be selected")
    @Valid
    private List<BookingServiceItemRequestDto> services;
}

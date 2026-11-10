package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.ServiceBookingRequestDto;
import com.serviceflow.backend.dto.ServiceBookingResponseDto;
import com.serviceflow.backend.entity.BookingStatus;
import com.serviceflow.backend.service.ServiceBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for service booking operations.
 * Provides endpoints for booking creation, retrieval, status updates, and cancellation.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class ServiceBookingController {

    private final ServiceBookingService serviceBookingService;

    /**
     * Create a new service booking.
     *
     * @param requestDto booking request data
     * @return created booking with 201 status
     */
    @PostMapping
    public ResponseEntity<ServiceBookingResponseDto> createBooking(
            @Valid @RequestBody ServiceBookingRequestDto requestDto) {
        log.info("POST /api/v1/bookings - Creating new booking");
        ServiceBookingResponseDto response = serviceBookingService.createBooking(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a booking by ID.
     *
     * @param id booking ID
     * @return booking data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceBookingResponseDto> getBookingById(@PathVariable Long id) {
        log.info("GET /api/v1/bookings/{} - Fetching booking", id);
        ServiceBookingResponseDto response = serviceBookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all bookings with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "bookingDateTime")
     * @param direction sort direction (default "DESC")
     * @return paginated list of bookings with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<ServiceBookingResponseDto>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "bookingDateTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("GET /api/v1/bookings - Fetching bookings (page: {}, size: {})", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceBookingResponseDto> response = serviceBookingService.getAllBookings(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bookings for a specific customer.
     *
     * @param customerId customer ID
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "bookingDateTime")
     * @param direction sort direction (default "DESC")
     * @return paginated list of customer bookings with 200 status
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getBookingsByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "bookingDateTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("GET /api/v1/bookings/customer/{} - Fetching customer bookings (page: {}, size: {})",
                customerId, page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceBookingResponseDto> response = serviceBookingService.getBookingsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bookings for a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "bookingDateTime")
     * @param direction sort direction (default "DESC")
     * @return paginated list of vehicle bookings with 200 status
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getBookingsByVehicleId(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "bookingDateTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("GET /api/v1/bookings/vehicle/{} - Fetching vehicle bookings (page: {}, size: {})",
                vehicleId, page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceBookingResponseDto> response = serviceBookingService.getBookingsByVehicleId(vehicleId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update booking status.
     *
     * @param id booking ID
     * @param statusRequest request body containing new status
     * @return updated booking with 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceBookingResponseDto> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusRequest) {

        log.info("PATCH /api/v1/bookings/{}/status - Updating booking status", id);

        String statusStr = statusRequest.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("Status field is required");
        }

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + statusStr);
        }

        ServiceBookingResponseDto response = serviceBookingService.updateBookingStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a booking (soft cancellation).
     *
     * @param id booking ID
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        log.info("DELETE /api/v1/bookings/{} - Cancelling booking", id);
        serviceBookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}

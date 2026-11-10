package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.InvalidStatusTransitionException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.CustomerRepository;
import com.serviceflow.backend.repository.ServiceBookingRepository;
import com.serviceflow.backend.repository.ServiceItemRepository;
import com.serviceflow.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service layer for managing service bookings.
 * Handles business logic for booking operations including validation,
 * status transitions, and price snapshot preservation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceBookingService {

    private final ServiceBookingRepository serviceBookingRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceItemRepository serviceItemRepository;

    // Thread-safe counter for booking number generation
    private static final AtomicLong bookingCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    /**
     * Create a new service booking.
     * Validates all business rules and creates booking with line item snapshots.
     *
     * @param requestDto booking request data
     * @return created booking
     * @throws ResourceNotFoundException if customer, vehicle, or service not found
     * @throws IllegalArgumentException if business rules are violated
     */
    @Transactional
    public ServiceBookingResponseDto createBooking(ServiceBookingRequestDto requestDto) {
        log.info("Creating booking for customer: {}, vehicle: {}", requestDto.getCustomerId(), requestDto.getVehicleId());

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + requestDto.getCustomerId()));

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + requestDto.getVehicleId()));

        // Validate vehicle belongs to customer
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Vehicle with id " + vehicle.getId() + " does not belong to customer " + customer.getId());
        }

        // Validate booking date is in future (additional runtime check beyond @Future validation)
        if (!requestDto.getBookingDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Booking date/time must be in the future");
        }

        // Consolidate duplicate services and validate
        Map<Long, Integer> consolidatedServices = consolidateServices(requestDto.getServices());

        // Validate all services exist and are active
        List<ServiceItem> serviceItems = validateAndFetchServices(consolidatedServices.keySet());

        // Generate unique booking number
        String bookingNumber = generateBookingNumber();

        // Calculate totals and create booking service items
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalDuration = 0;
        List<BookingServiceItem> bookingServiceItems = new ArrayList<>();

        for (ServiceItem serviceItem : serviceItems) {
            Integer quantity = consolidatedServices.get(serviceItem.getId());

            // Create snapshot of service details
            BookingServiceItem lineItem = BookingServiceItem.builder()
                    .serviceItem(serviceItem)
                    .serviceCodeSnapshot(serviceItem.getServiceCode())
                    .serviceNameSnapshot(serviceItem.getName())
                    .unitPriceSnapshot(serviceItem.getBasePrice())
                    .estimatedDurationMinutesSnapshot(serviceItem.getEstimatedDurationMinutes())
                    .quantity(quantity)
                    .build();

            // Calculate line total
            lineItem.calculateLineTotal();

            bookingServiceItems.add(lineItem);

            // Accumulate totals
            totalPrice = totalPrice.add(lineItem.getLineTotal());
            totalDuration += lineItem.getEstimatedDurationMinutesSnapshot() * quantity;
        }

        // Create booking
        ServiceBooking booking = ServiceBooking.builder()
                .bookingNumber(bookingNumber)
                .customer(customer)
                .vehicle(vehicle)
                .bookingDateTime(requestDto.getBookingDateTime())
                .customerNotes(requestDto.getCustomerNotes())
                .bookingStatus(BookingStatus.REQUESTED)
                .estimatedTotalPrice(totalPrice)
                .estimatedTotalDurationMinutes(totalDuration)
                .build();

        // Add line items to booking
        for (BookingServiceItem lineItem : bookingServiceItems) {
            booking.addBookingServiceItem(lineItem);
        }

        // Save booking (cascades to line items)
        ServiceBooking savedBooking = serviceBookingRepository.save(booking);

        log.info("Booking created successfully with booking number: {}", savedBooking.getBookingNumber());

        return mapToResponseDto(savedBooking);
    }

    /**
     * Get booking by ID.
     */
    @Transactional(readOnly = true)
    public ServiceBookingResponseDto getBookingById(Long id) {
        log.info("Fetching booking with id: {}", id);

        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        return mapToResponseDto(booking);
    }

    /**
     * Get all bookings with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getAllBookings(Pageable pageable) {
        log.info("Fetching bookings with pagination: {}", pageable);

        Page<ServiceBooking> bookings = serviceBookingRepository.findAll(pageable);
        return bookings.map(this::mapToResponseDto);
    }

    /**
     * Get bookings for a specific customer.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getBookingsByCustomerId(Long customerId, Pageable pageable) {
        log.info("Fetching bookings for customer: {}", customerId);

        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        Page<ServiceBooking> bookings = serviceBookingRepository.findByCustomerId(customerId, pageable);
        return bookings.map(this::mapToResponseDto);
    }

    /**
     * Get bookings for a specific vehicle.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getBookingsByVehicleId(Long vehicleId, Pageable pageable) {
        log.info("Fetching bookings for vehicle: {}", vehicleId);

        // Verify vehicle exists
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
        }

        Page<ServiceBooking> bookings = serviceBookingRepository.findByVehicleId(vehicleId, pageable);
        return bookings.map(this::mapToResponseDto);
    }

    /**
     * Update booking status with transition validation.
     */
    @Transactional
    public ServiceBookingResponseDto updateBookingStatus(Long id, BookingStatus newStatus) {
        log.info("Updating booking {} status to {}", id, newStatus);

        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        BookingStatus currentStatus = booking.getBookingStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);

        booking.setBookingStatus(newStatus);
        ServiceBooking updatedBooking = serviceBookingRepository.save(booking);

        log.info("Booking {} status updated to {}", id, newStatus);

        return mapToResponseDto(updatedBooking);
    }

    /**
     * Cancel a booking (soft cancellation - preserves history).
     */
    @Transactional
    public void cancelBooking(Long id) {
        log.info("Cancelling booking: {}", id);

        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        BookingStatus currentStatus = booking.getBookingStatus();

        // Only allow cancellation from REQUESTED or CONFIRMED states
        if (currentStatus == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Booking is already cancelled");
        }

        if (currentStatus == BookingStatus.CHECKED_IN) {
            throw new InvalidStatusTransitionException("Cannot cancel a booking that has been checked in");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        serviceBookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", id);
    }

    /**
     * Consolidate duplicate services in request and sum their quantities.
     */
    private Map<Long, Integer> consolidateServices(List<BookingServiceItemRequestDto> services) {
        Map<Long, Integer> consolidated = new HashMap<>();

        for (BookingServiceItemRequestDto service : services) {
            consolidated.merge(service.getServiceId(), service.getQuantity(), Integer::sum);
        }

        return consolidated;
    }

    /**
     * Validate all services exist and are active.
     */
    private List<ServiceItem> validateAndFetchServices(Iterable<Long> serviceIds) {
        List<ServiceItem> serviceItems = serviceItemRepository.findAllById(serviceIds);

        // Check if all requested services were found
        List<Long> foundIds = serviceItems.stream()
                .map(ServiceItem::getId)
                .collect(Collectors.toList());

        for (Long requestedId : serviceIds) {
            if (!foundIds.contains(requestedId)) {
                throw new ResourceNotFoundException("Service not found with id: " + requestedId);
            }
        }

        // Check if all services are active
        List<Long> inactiveServiceIds = serviceItems.stream()
                .filter(service -> !service.getActive())
                .map(ServiceItem::getId)
                .collect(Collectors.toList());

        if (!inactiveServiceIds.isEmpty()) {
            throw new IllegalArgumentException("Cannot book inactive services: " + inactiveServiceIds);
        }

        return serviceItems;
    }

    /**
     * Validate booking status transition.
     */
    private void validateStatusTransition(BookingStatus current, BookingStatus target) {
        if (current == target) {
            throw new InvalidStatusTransitionException("Booking is already in " + target + " status");
        }

        boolean valid = switch (current) {
            case REQUESTED -> target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED;
            case CONFIRMED -> target == BookingStatus.CHECKED_IN || target == BookingStatus.CANCELLED;
            case CANCELLED, CHECKED_IN -> false; // Terminal states
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", current, target));
        }
    }

    /**
     * Generate unique booking number.
     * Format: BK-YYYYMMDD-XXXXX
     */
    private String generateBookingNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = bookingCounter.incrementAndGet();
        String bookingNumber = String.format("BK-%s-%05d", datePart, counter % 100000);

        // Ensure uniqueness (handle unlikely collision)
        while (serviceBookingRepository.existsByBookingNumber(bookingNumber)) {
            counter = bookingCounter.incrementAndGet();
            bookingNumber = String.format("BK-%s-%05d", datePart, counter % 100000);
        }

        return bookingNumber;
    }

    /**
     * Map ServiceBooking entity to response DTO.
     */
    private ServiceBookingResponseDto mapToResponseDto(ServiceBooking booking) {
        Customer customer = booking.getCustomer();
        Vehicle vehicle = booking.getVehicle();

        CustomerSummaryDto customerDto = CustomerSummaryDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .build();

        VehicleSummaryDto vehicleDto = VehicleSummaryDto.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .manufacturingYear(vehicle.getManufacturingYear())
                .build();

        List<BookingServiceItemResponseDto> serviceItems = booking.getBookingServiceItems().stream()
                .map(item -> BookingServiceItemResponseDto.builder()
                        .id(item.getId())
                        .serviceItemId(item.getServiceItem().getId())
                        .serviceCode(item.getServiceCodeSnapshot())
                        .serviceName(item.getServiceNameSnapshot())
                        .unitPrice(item.getUnitPriceSnapshot())
                        .estimatedDurationMinutes(item.getEstimatedDurationMinutesSnapshot())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        return ServiceBookingResponseDto.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .customer(customerDto)
                .vehicle(vehicleDto)
                .bookingDateTime(booking.getBookingDateTime())
                .customerNotes(booking.getCustomerNotes())
                .bookingStatus(booking.getBookingStatus())
                .estimatedTotalPrice(booking.getEstimatedTotalPrice())
                .estimatedTotalDurationMinutes(booking.getEstimatedTotalDurationMinutes())
                .services(serviceItems)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}

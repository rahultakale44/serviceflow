package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.InvalidStatusTransitionException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.CustomerRepository;
import com.serviceflow.backend.repository.ServiceBookingRepository;
import com.serviceflow.backend.repository.ServiceItemRepository;
import com.serviceflow.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceBookingService.
 * Tests booking creation, validation, status transitions, and retrieval operations.
 */
@ExtendWith(MockitoExtension.class)
class ServiceBookingServiceTest {

    @Mock
    private ServiceBookingRepository serviceBookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @InjectMocks
    private ServiceBookingService serviceBookingService;

    private Customer testCustomer;
    private Vehicle testVehicle;
    private ServiceItem testService;
    private ServiceBookingRequestDto testRequest;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john@example.com")
                .build();

        testVehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .customer(testCustomer)
                .build();

        testService = ServiceItem.builder()
                .id(1L)
                .serviceCode("OC001")
                .name("Oil Change")
                .basePrice(BigDecimal.valueOf(50.00))
                .estimatedDurationMinutes(30)
                .active(true)
                .build();

        BookingServiceItemRequestDto serviceRequest = BookingServiceItemRequestDto.builder()
                .serviceId(1L)
                .quantity(1)
                .build();

        testRequest = ServiceBookingRequestDto.builder()
                .customerId(1L)
                .vehicleId(1L)
                .bookingDateTime(LocalDateTime.now().plusDays(1))
                .customerNotes("Regular service")
                .services(List.of(serviceRequest))
                .build();
    }

    @Test
    void createBooking_Success() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(serviceItemRepository.findAllById(any())).thenReturn(List.of(testService));
        when(serviceBookingRepository.existsByBookingNumber(any())).thenReturn(false);
        when(serviceBookingRepository.save(any(ServiceBooking.class))).thenAnswer(invocation -> {
            ServiceBooking booking = invocation.getArgument(0);
            booking.setId(1L);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            return booking;
        });

        // Act
        ServiceBookingResponseDto response = serviceBookingService.createBooking(testRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBookingNumber());
        assertEquals(BookingStatus.REQUESTED, response.getBookingStatus());
        assertEquals(BigDecimal.valueOf(50.00), response.getEstimatedTotalPrice());
        assertEquals(30, response.getEstimatedTotalDurationMinutes());
        assertEquals(1, response.getServices().size());

        verify(serviceBookingRepository, times(1)).save(any(ServiceBooking.class));
    }

    @Test
    void createBooking_WithMultipleServices() {
        // Arrange
        ServiceItem service2 = ServiceItem.builder()
                .id(2L)
                .serviceCode("BI001")
                .name("Brake Inspection")
                .basePrice(BigDecimal.valueOf(40.00))
                .estimatedDurationMinutes(45)
                .active(true)
                .build();

        List<BookingServiceItemRequestDto> services = List.of(
                BookingServiceItemRequestDto.builder().serviceId(1L).quantity(1).build(),
                BookingServiceItemRequestDto.builder().serviceId(2L).quantity(1).build()
        );

        testRequest.setServices(services);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(serviceItemRepository.findAllById(any())).thenReturn(List.of(testService, service2));
        when(serviceBookingRepository.existsByBookingNumber(any())).thenReturn(false);
        when(serviceBookingRepository.save(any(ServiceBooking.class))).thenAnswer(invocation -> {
            ServiceBooking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // Act
        ServiceBookingResponseDto response = serviceBookingService.createBooking(testRequest);

        // Assert
        assertEquals(2, response.getServices().size());
        assertEquals(BigDecimal.valueOf(90.00), response.getEstimatedTotalPrice());
        assertEquals(75, response.getEstimatedTotalDurationMinutes());
    }

    @Test
    void createBooking_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingService.createBooking(testRequest)
        );

        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void createBooking_VehicleNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingService.createBooking(testRequest)
        );

        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void createBooking_VehicleBelongsToAnotherCustomer() {
        // Arrange
        Customer anotherCustomer = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .customer(anotherCustomer)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            serviceBookingService.createBooking(testRequest)
        );

        assertTrue(exception.getMessage().contains("does not belong to customer"));
        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ServiceNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(serviceItemRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingService.createBooking(testRequest)
        );

        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void createBooking_InactiveServiceRejected() {
        // Arrange
        testService.setActive(false);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(serviceItemRepository.findAllById(any())).thenReturn(List.of(testService));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            serviceBookingService.createBooking(testRequest)
        );

        assertTrue(exception.getMessage().contains("Cannot book inactive services"));
        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ConsolidatesDuplicateServices() {
        // Arrange
        List<BookingServiceItemRequestDto> services = List.of(
                BookingServiceItemRequestDto.builder().serviceId(1L).quantity(2).build(),
                BookingServiceItemRequestDto.builder().serviceId(1L).quantity(3).build()
        );

        testRequest.setServices(services);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(serviceItemRepository.findAllById(any())).thenReturn(List.of(testService));
        when(serviceBookingRepository.existsByBookingNumber(any())).thenReturn(false);
        when(serviceBookingRepository.save(any(ServiceBooking.class))).thenAnswer(invocation -> {
            ServiceBooking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // Act
        ServiceBookingResponseDto response = serviceBookingService.createBooking(testRequest);

        // Assert
        assertEquals(1, response.getServices().size());
        assertEquals(5, response.getServices().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(250.00), response.getEstimatedTotalPrice());
        assertEquals(150, response.getEstimatedTotalDurationMinutes());
    }

    @Test
    void getBookingById_Success() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Act
        ServiceBookingResponseDto response = serviceBookingService.getBookingById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("BK-20261110-00001", response.getBookingNumber());
    }

    @Test
    void getBookingById_NotFound() {
        // Arrange
        when(serviceBookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingService.getBookingById(999L)
        );
    }

    @Test
    void getAllBookings_Success() {
        // Arrange
        List<ServiceBooking> bookings = List.of(createTestBooking());
        Page<ServiceBooking> bookingPage = new PageImpl<>(bookings);
        Pageable pageable = PageRequest.of(0, 20);

        when(serviceBookingRepository.findAll(pageable)).thenReturn(bookingPage);

        // Act
        Page<ServiceBookingResponseDto> response = serviceBookingService.getAllBookings(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getBookingsByCustomerId_Success() {
        // Arrange
        List<ServiceBooking> bookings = List.of(createTestBooking());
        Page<ServiceBooking> bookingPage = new PageImpl<>(bookings);
        Pageable pageable = PageRequest.of(0, 20);

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(serviceBookingRepository.findByCustomerId(1L, pageable)).thenReturn(bookingPage);

        // Act
        Page<ServiceBookingResponseDto> response = serviceBookingService.getBookingsByCustomerId(1L, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getBookingsByCustomerId_CustomerNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingService.getBookingsByCustomerId(999L, pageable)
        );
    }

    @Test
    void getBookingsByVehicleId_Success() {
        // Arrange
        List<ServiceBooking> bookings = List.of(createTestBooking());
        Page<ServiceBooking> bookingPage = new PageImpl<>(bookings);
        Pageable pageable = PageRequest.of(0, 20);

        when(vehicleRepository.existsById(1L)).thenReturn(true);
        when(serviceBookingRepository.findByVehicleId(1L, pageable)).thenReturn(bookingPage);

        // Act
        Page<ServiceBookingResponseDto> response = serviceBookingService.getBookingsByVehicleId(1L, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void updateBookingStatus_RequestedToConfirmed() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        booking.setBookingStatus(BookingStatus.REQUESTED);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(serviceBookingRepository.save(any(ServiceBooking.class))).thenReturn(booking);

        // Act
        ServiceBookingResponseDto response = serviceBookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED);

        // Assert
        assertEquals(BookingStatus.CONFIRMED, response.getBookingStatus());
        verify(serviceBookingRepository, times(1)).save(any(ServiceBooking.class));
    }

    @Test
    void updateBookingStatus_InvalidTransition() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        booking.setBookingStatus(BookingStatus.CANCELLED);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class, () -> 
            serviceBookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED)
        );

        verify(serviceBookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_Success() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        booking.setBookingStatus(BookingStatus.REQUESTED);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(serviceBookingRepository.save(any(ServiceBooking.class))).thenReturn(booking);

        // Act
        serviceBookingService.cancelBooking(1L);

        // Assert
        verify(serviceBookingRepository, times(1)).save(any(ServiceBooking.class));
    }

    @Test
    void cancelBooking_AlreadyCancelled() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        booking.setBookingStatus(BookingStatus.CANCELLED);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class, () -> 
            serviceBookingService.cancelBooking(1L)
        );
    }

    @Test
    void cancelBooking_AlreadyCheckedIn() {
        // Arrange
        ServiceBooking booking = createTestBooking();
        booking.setBookingStatus(BookingStatus.CHECKED_IN);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class, () -> 
            serviceBookingService.cancelBooking(1L)
        );
    }

    private ServiceBooking createTestBooking() {
        BookingServiceItem lineItem = BookingServiceItem.builder()
                .id(1L)
                .serviceItem(testService)
                .serviceCodeSnapshot("OC001")
                .serviceNameSnapshot("Oil Change")
                .unitPriceSnapshot(BigDecimal.valueOf(50.00))
                .estimatedDurationMinutesSnapshot(30)
                .quantity(1)
                .lineTotal(BigDecimal.valueOf(50.00))
                .build();

        ServiceBooking booking = ServiceBooking.builder()
                .id(1L)
                .bookingNumber("BK-20261110-00001")
                .customer(testCustomer)
                .vehicle(testVehicle)
                .bookingDateTime(LocalDateTime.now().plusDays(1))
                .customerNotes("Regular service")
                .bookingStatus(BookingStatus.REQUESTED)
                .estimatedTotalPrice(BigDecimal.valueOf(50.00))
                .estimatedTotalDurationMinutes(30)
                .bookingServiceItems(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        booking.addBookingServiceItem(lineItem);

        return booking;
    }
}

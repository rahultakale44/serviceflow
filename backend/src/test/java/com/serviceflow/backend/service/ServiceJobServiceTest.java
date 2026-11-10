package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.*;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceJobServiceTest {

    @Mock
    private ServiceJobRepository serviceJobRepository;

    @Mock
    private JobInspectionRepository jobInspectionRepository;

    @Mock
    private ServiceBookingRepository serviceBookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ServiceJobService serviceJobService;

    private Customer customer;
    private Vehicle vehicle;
    private ServiceBooking booking;
    private ServiceJob serviceJob;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .manufacturingYear(2020)
                .vin("VIN123456789")
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        booking = ServiceBooking.builder()
                .id(1L)
                .customer(customer)
                .vehicle(vehicle)
                .bookingDateTime(LocalDateTime.now())
                .bookingStatus(BookingStatus.CHECKED_IN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        serviceJob = ServiceJob.builder()
                .id(1L)
                .jobCardNumber("JC-20261110-00001")
                .booking(booking)
                .customer(customer)
                .vehicle(vehicle)
                .jobStatus(JobStatus.CHECKED_IN)
                .complaintDescription("Engine noise")
                .inspectionNotes("Initial inspection notes")
                .technicianNotes("Technician notes")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createJobFromBooking_Success() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(
                1L,
                "Engine noise",
                "Initial inspection notes"
        );

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(serviceJobRepository.existsByBookingId(1L)).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(serviceJobRepository.findAll()).thenReturn(new ArrayList<>());
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.createJobFromBooking(requestDto);

        assertNotNull(response);
        assertEquals("JC-20261110-00001", response.jobCardNumber());
        assertEquals(1L, response.bookingId());
        assertEquals(JobStatus.CHECKED_IN, response.jobStatus());
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void createJobFromBooking_BookingNotFound() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(999L, "Test", null);

        when(serviceBookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.createJobFromBooking(requestDto));
    }

    @Test
    void createJobFromBooking_BookingNotCheckedIn() {
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(1L, "Test", null);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> serviceJobService.createJobFromBooking(requestDto));

        assertTrue(exception.getMessage().contains("CHECKED_IN status"));
    }

    @Test
    void createJobFromBooking_DuplicateJob() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(1L, "Test", null);

        when(serviceBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(serviceJobRepository.existsByBookingId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> serviceJobService.createJobFromBooking(requestDto));
    }

    @Test
    void getJobById_Success() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L)).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.getJobById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("JC-20261110-00001", response.jobCardNumber());
    }

    @Test
    void getJobById_NotFound() {
        when(serviceJobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.getJobById(999L));
    }

    @Test
    void getJobByJobCardNumber_Success() {
        when(serviceJobRepository.findByJobCardNumber("JC-20261110-00001"))
                .thenReturn(Optional.of(serviceJob));
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L)).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.getJobByJobCardNumber("JC-20261110-00001");

        assertNotNull(response);
        assertEquals("JC-20261110-00001", response.jobCardNumber());
    }

    @Test
    void getJobByJobCardNumber_NotFound() {
        when(serviceJobRepository.findByJobCardNumber("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.getJobByJobCardNumber("INVALID"));
    }

    @Test
    void getJobByBookingId_Success() {
        when(serviceJobRepository.findByBookingId(1L)).thenReturn(Optional.of(serviceJob));
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L)).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.getJobByBookingId(1L);

        assertNotNull(response);
        assertEquals(1L, response.bookingId());
    }

    @Test
    void getJobByBookingId_NotFound() {
        when(serviceJobRepository.findByBookingId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.getJobByBookingId(999L));
    }

    @Test
    void getAllJobs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceJob> jobPage = new PageImpl<>(List.of(serviceJob));

        when(serviceJobRepository.findAll(pageable)).thenReturn(jobPage);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        Page<ServiceJobResponseDto> response = serviceJobService.getAllJobs(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("JC-20261110-00001", response.getContent().get(0).jobCardNumber());
    }

    @Test
    void getJobsByVehicleId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceJob> jobPage = new PageImpl<>(List.of(serviceJob));

        when(vehicleRepository.existsById(1L)).thenReturn(true);
        when(serviceJobRepository.findByVehicleId(1L, pageable)).thenReturn(jobPage);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        Page<ServiceJobResponseDto> response = serviceJobService.getJobsByVehicleId(1L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getJobsByVehicleId_VehicleNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(vehicleRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.getJobsByVehicleId(999L, pageable));
    }

    @Test
    void updateJobStatus_ValidTransition_CheckedInToInspection() {
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.INSPECTION, "Moving to inspection");

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.updateJobStatus(1L, updateDto);

        assertNotNull(response);
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_ValidTransition_QualityCheckToInService() {
        serviceJob.setJobStatus(JobStatus.QUALITY_CHECK);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.IN_SERVICE, "Rework required");

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.updateJobStatus(1L, updateDto);

        assertNotNull(response);
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_InvalidTransition() {
        serviceJob.setJobStatus(JobStatus.CHECKED_IN);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.DELIVERED, "Invalid jump");

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> serviceJobService.updateJobStatus(1L, updateDto));

        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void updateJobStatus_UpdatesStartedAtForInService() {
        serviceJob.setJobStatus(JobStatus.APPROVED);
        serviceJob.setStartedAt(null);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.IN_SERVICE, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenAnswer(invocation -> {
            ServiceJob saved = invocation.getArgument(0);
            assertNotNull(saved.getStartedAt());
            return saved;
        });
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        serviceJobService.updateJobStatus(1L, updateDto);

        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_UpdatesCompletedAtForQualityCheck() {
        serviceJob.setJobStatus(JobStatus.IN_SERVICE);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.QUALITY_CHECK, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenAnswer(invocation -> {
            ServiceJob saved = invocation.getArgument(0);
            assertNotNull(saved.getCompletedAt());
            return saved;
        });
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        serviceJobService.updateJobStatus(1L, updateDto);

        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_UpdatesDeliveredAtForDelivered() {
        serviceJob.setJobStatus(JobStatus.READY_FOR_DELIVERY);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.DELIVERED, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenAnswer(invocation -> {
            ServiceJob saved = invocation.getArgument(0);
            assertNotNull(saved.getDeliveredAt());
            return saved;
        });
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        serviceJobService.updateJobStatus(1L, updateDto);

        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobNotes_Success() {
        JobNotesUpdateDto notesDto = new JobNotesUpdateDto(
                "Updated inspection notes",
                "Updated technician notes"
        );

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(anyLong())).thenReturn(new ArrayList<>());

        ServiceJobResponseDto response = serviceJobService.updateJobNotes(1L, notesDto);

        assertNotNull(response);
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobNotes_NotFound() {
        JobNotesUpdateDto notesDto = new JobNotesUpdateDto("Test", "Test");

        when(serviceJobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.updateJobNotes(999L, notesDto));
    }

    @Test
    void addInspection_Success() {
        JobInspectionRequestDto requestDto = new JobInspectionRequestDto(
                InspectionCategory.ENGINE,
                "Oil Level",
                InspectionCondition.GOOD,
                "Oil level is normal"
        );

        JobInspection inspection = JobInspection.builder()
                .id(1L)
                .job(serviceJob)
                .inspectionCategory(InspectionCategory.ENGINE)
                .itemName("Oil Level")
                .condition(InspectionCondition.GOOD)
                .notes("Oil level is normal")
                .inspectedAt(LocalDateTime.now())
                .build();

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(jobInspectionRepository.save(any(JobInspection.class))).thenReturn(inspection);

        JobInspectionResponseDto response = serviceJobService.addInspection(1L, requestDto);

        assertNotNull(response);
        assertEquals(InspectionCategory.ENGINE, response.category());
        assertEquals("Oil Level", response.itemName());
        assertEquals(InspectionCondition.GOOD, response.condition());
        verify(jobInspectionRepository, times(1)).save(any(JobInspection.class));
    }

    @Test
    void addInspection_JobNotFound() {
        JobInspectionRequestDto requestDto = new JobInspectionRequestDto(
                InspectionCategory.ENGINE, "Test", InspectionCondition.GOOD, null);

        when(serviceJobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.addInspection(999L, requestDto));
    }

    @Test
    void getJobInspections_Success() {
        JobInspection inspection = JobInspection.builder()
                .id(1L)
                .job(serviceJob)
                .inspectionCategory(InspectionCategory.ENGINE)
                .itemName("Oil Level")
                .condition(InspectionCondition.GOOD)
                .inspectedAt(LocalDateTime.now())
                .build();

        when(serviceJobRepository.existsById(1L)).thenReturn(true);
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L))
                .thenReturn(List.of(inspection));

        List<JobInspectionResponseDto> response = serviceJobService.getJobInspections(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(InspectionCategory.ENGINE, response.get(0).category());
    }

    @Test
    void getJobInspections_JobNotFound() {
        when(serviceJobRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobService.getJobInspections(999L));
    }
}

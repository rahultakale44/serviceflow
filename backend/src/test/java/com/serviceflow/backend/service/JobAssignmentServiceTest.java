package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.JobAssignmentRequestDto;
import com.serviceflow.backend.dto.JobAssignmentResponseDto;
import com.serviceflow.backend.dto.ReassignmentRequestDto;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.AssignmentConflictException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobAssignmentService.
 */
@ExtendWith(MockitoExtension.class)
class JobAssignmentServiceTest {

    @Mock
    private JobAssignmentRepository assignmentRepository;

    @Mock
    private ServiceJobRepository serviceJobRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private ServiceBayRepository serviceBayRepository;

    @Mock
    private RepairEstimateRepository repairEstimateRepository;

    @InjectMocks
    private JobAssignmentService assignmentService;

    private JobAssignmentRequestDto requestDto;
    private ServiceJob serviceJob;
    private Technician technician;
    private ServiceBay serviceBay;
    private JobAssignment assignment;
    private Customer customer;
    private Vehicle vehicle;
    private ServiceBooking booking;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = now.plusHours(2);
        LocalDateTime scheduledEnd = scheduledStart.plusHours(2);

        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john@example.com")
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .customer(customer)
                .build();

        booking = ServiceBooking.builder()
                .id(1L)
                .bookingNumber("BK-20261110-00001")
                .customer(customer)
                .vehicle(vehicle)
                .bookingStatus(BookingStatus.CHECKED_IN)
                .build();

        serviceJob = ServiceJob.builder()
                .id(1L)
                .jobCardNumber("JC-20261110-00001")
                .booking(booking)
                .customer(customer)
                .vehicle(vehicle)
                .jobStatus(JobStatus.INSPECTION)
                .build();

        technician = Technician.builder()
                .id(1L)
                .technicianCode("TECH001")
                .firstName("Mike")
                .lastName("Smith")
                .specialization(TechnicianSpecialization.ENGINE)
                .active(true)
                .status(TechnicianStatus.AVAILABLE)
                .build();

        serviceBay = ServiceBay.builder()
                .id(1L)
                .bayCode("BAY01")
                .bayName("Engine Bay 1")
                .bayType(BayType.ENGINE)
                .active(true)
                .build();

        requestDto = JobAssignmentRequestDto.builder()
                .jobId(1L)
                .technicianId(1L)
                .bayId(1L)
                .scheduledStart(scheduledStart)
                .scheduledEnd(scheduledEnd)
                .notes("Assign for engine inspection")
                .build();

        assignment = JobAssignment.builder()
                .id(1L)
                .job(serviceJob)
                .technician(technician)
                .bay(serviceBay)
                .scheduledStart(scheduledStart)
                .scheduledEnd(scheduledEnd)
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .notes("Assign for engine inspection")
                .assignedAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void createAssignment_Success() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(assignmentRepository.hasOverlappingTechnicianAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.hasOverlappingBayAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.hasOverlappingJobAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.save(any(JobAssignment.class))).thenReturn(assignment);

        JobAssignmentResponseDto response = assignmentService.createAssignment(requestDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("JC-20261110-00001", response.getJobCardNumber());
        assertEquals("TECH001", response.getTechnicianCode());
        assertEquals("BAY01", response.getBayCode());
        verify(assignmentRepository).save(any(JobAssignment.class));
    }

    @Test
    void createAssignment_InvalidSchedule_ThrowsException() {
        JobAssignmentRequestDto invalidDto = JobAssignmentRequestDto.builder()
                .jobId(1L)
                .technicianId(1L)
                .bayId(1L)
                .scheduledStart(LocalDateTime.now())
                .scheduledEnd(LocalDateTime.now().minusHours(1)) // End before start
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(invalidDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_JobNotFound_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_TechnicianNotFound_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_InactiveTechnician_ThrowsException() {
        technician.setActive(false);
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_BayNotFound_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_InactiveBay_ThrowsException() {
        serviceBay.setActive(false);
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_TechnicianConflict_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(assignmentRepository.hasOverlappingTechnicianAssignment(anyLong(), any(), any()))
                .thenReturn(true);

        assertThrows(AssignmentConflictException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_BayConflict_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(assignmentRepository.hasOverlappingTechnicianAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.hasOverlappingBayAssignment(anyLong(), any(), any()))
                .thenReturn(true);

        assertThrows(AssignmentConflictException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_JobConflict_ThrowsException() {
        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(assignmentRepository.hasOverlappingTechnicianAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.hasOverlappingBayAssignment(anyLong(), any(), any()))
                .thenReturn(false);
        when(assignmentRepository.hasOverlappingJobAssignment(anyLong(), any(), any()))
                .thenReturn(true);

        assertThrows(AssignmentConflictException.class,
                () -> assignmentService.createAssignment(requestDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void getAssignmentById_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        JobAssignmentResponseDto response = assignmentService.getAssignmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("JC-20261110-00001", response.getJobCardNumber());
    }

    @Test
    void getAssignmentById_NotFound_ThrowsException() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.getAssignmentById(1L));
    }

    @Test
    void getAllAssignments_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JobAssignment> page = new PageImpl<>(List.of(assignment));
        when(assignmentRepository.findAll(pageable)).thenReturn(page);

        Page<JobAssignmentResponseDto> result = assignmentService.getAllAssignments(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAssignmentsByJob_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JobAssignment> page = new PageImpl<>(List.of(assignment));
        when(serviceJobRepository.existsById(1L)).thenReturn(true);
        when(assignmentRepository.findByJobId(1L, pageable)).thenReturn(page);

        Page<JobAssignmentResponseDto> result = assignmentService.getAssignmentsByJob(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAssignmentsByTechnician_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JobAssignment> page = new PageImpl<>(List.of(assignment));
        when(technicianRepository.existsById(1L)).thenReturn(true);
        when(assignmentRepository.findByTechnicianId(1L, pageable)).thenReturn(page);

        Page<JobAssignmentResponseDto> result = assignmentService
                .getAssignmentsByTechnician(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void completeAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(JobAssignment.class))).thenReturn(assignment);

        JobAssignmentResponseDto response = assignmentService.completeAssignment(1L);

        assertNotNull(response);
        verify(assignmentRepository).save(any(JobAssignment.class));
    }

    @Test
    void completeAssignment_NotActive_ThrowsException() {
        assignment.setAssignmentStatus(AssignmentStatus.COMPLETED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.completeAssignment(1L));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void cancelAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(JobAssignment.class))).thenReturn(assignment);

        JobAssignmentResponseDto response = assignmentService.cancelAssignment(1L);

        assertNotNull(response);
        verify(assignmentRepository).save(any(JobAssignment.class));
    }

    @Test
    void reassignJob_Success() {
        Technician newTechnician = Technician.builder()
                .id(2L)
                .technicianCode("TECH002")
                .firstName("Jane")
                .lastName("Doe")
                .specialization(TechnicianSpecialization.BRAKES)
                .active(true)
                .build();

        ServiceBay newBay = ServiceBay.builder()
                .id(2L)
                .bayCode("BAY02")
                .bayName("General Bay 2")
                .bayType(BayType.GENERAL)
                .active(true)
                .build();

        ReassignmentRequestDto reassignDto = ReassignmentRequestDto.builder()
                .technicianId(2L)
                .bayId(2L)
                .scheduledStart(LocalDateTime.now().plusHours(3))
                .scheduledEnd(LocalDateTime.now().plusHours(5))
                .notes("Reassigned to different technician")
                .build();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(technicianRepository.findById(2L)).thenReturn(Optional.of(newTechnician));
        when(serviceBayRepository.findById(2L)).thenReturn(Optional.of(newBay));
        when(assignmentRepository.findByJobId(1L)).thenReturn(List.of());
        when(assignmentRepository.findByTechnicianIdAndAssignmentStatus(anyLong(), any()))
                .thenReturn(List.of());
        when(assignmentRepository.findByBayIdAndAssignmentStatus(anyLong(), any()))
                .thenReturn(List.of());
        when(assignmentRepository.save(any(JobAssignment.class))).thenReturn(assignment);

        JobAssignmentResponseDto response = assignmentService.reassignJob(1L, reassignDto);

        assertNotNull(response);
        verify(assignmentRepository, times(2)).save(any(JobAssignment.class)); // Cancel old + create new
    }

    @Test
    void reassignJob_NotActive_ThrowsException() {
        assignment.setAssignmentStatus(AssignmentStatus.COMPLETED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        ReassignmentRequestDto reassignDto = ReassignmentRequestDto.builder()
                .technicianId(2L)
                .bayId(2L)
                .scheduledStart(LocalDateTime.now().plusHours(3))
                .scheduledEnd(LocalDateTime.now().plusHours(5))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.reassignJob(1L, reassignDto));
        verify(assignmentRepository, never()).save(any());
    }
}

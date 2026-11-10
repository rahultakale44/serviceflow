package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.InspectionCategory;
import com.serviceflow.backend.entity.InspectionCondition;
import com.serviceflow.backend.entity.JobStatus;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.ServiceJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceJobControllerTest {

    @Mock
    private ServiceJobService serviceJobService;

    @InjectMocks
    private ServiceJobController serviceJobController;

    private ServiceJobResponseDto jobResponseDto;

    @BeforeEach
    void setUp() {
        jobResponseDto = new ServiceJobResponseDto(
                1L,
                "JC-20261110-00001",
                1L,
                1L,
                "John Doe",
                "1234567890",
                1L,
                "ABC123",
                "Toyota",
                "Camry",
                JobStatus.CHECKED_IN,
                "Engine noise",
                "Initial inspection notes",
                "Technician notes",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null,
                List.of()
        );
    }

    @Test
    void createJob_Success() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(
                1L, "Engine noise", "Initial inspection notes");

        when(serviceJobService.createJobFromBooking(any(ServiceJobRequestDto.class)))
                .thenReturn(jobResponseDto);

        ResponseEntity<ServiceJobResponseDto> response = serviceJobController.createJob(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("JC-20261110-00001", response.getBody().jobCardNumber());
        assertEquals(1L, response.getBody().bookingId());
        assertEquals(JobStatus.CHECKED_IN, response.getBody().jobStatus());
        verify(serviceJobService, times(1)).createJobFromBooking(any(ServiceJobRequestDto.class));
    }

    @Test
    void createJob_BookingNotFound() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(999L, "Test", null);

        when(serviceJobService.createJobFromBooking(any(ServiceJobRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Booking not found with ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.createJob(requestDto));
    }

    @Test
    void createJob_DuplicateJob() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(1L, "Test", null);

        when(serviceJobService.createJobFromBooking(any(ServiceJobRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Job card already exists for booking ID: 1"));

        assertThrows(DuplicateResourceException.class,
                () -> serviceJobController.createJob(requestDto));
    }

    @Test
    void createJob_InvalidBookingStatus() {
        ServiceJobRequestDto requestDto = new ServiceJobRequestDto(1L, "Test", null);

        when(serviceJobService.createJobFromBooking(any(ServiceJobRequestDto.class)))
                .thenThrow(new IllegalStateException("Job card can only be created for bookings in CHECKED_IN status"));

        assertThrows(IllegalStateException.class,
                () -> serviceJobController.createJob(requestDto));
    }

    @Test
    void getJobById_Success() {
        when(serviceJobService.getJobById(1L)).thenReturn(jobResponseDto);

        ResponseEntity<ServiceJobResponseDto> response = serviceJobController.getJobById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("JC-20261110-00001", response.getBody().jobCardNumber());
    }

    @Test
    void getJobById_NotFound() {
        when(serviceJobService.getJobById(999L))
                .thenThrow(new ResourceNotFoundException("Service job not found with ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.getJobById(999L));
    }

    @Test
    void getJobByJobCardNumber_Success() {
        when(serviceJobService.getJobByJobCardNumber("JC-20261110-00001"))
                .thenReturn(jobResponseDto);

        ResponseEntity<ServiceJobResponseDto> response = 
                serviceJobController.getJobByJobCardNumber("JC-20261110-00001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JC-20261110-00001", response.getBody().jobCardNumber());
    }

    @Test
    void getJobByJobCardNumber_NotFound() {
        when(serviceJobService.getJobByJobCardNumber("INVALID"))
                .thenThrow(new ResourceNotFoundException("Service job not found with job card number: INVALID"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.getJobByJobCardNumber("INVALID"));
    }

    @Test
    void getJobByBookingId_Success() {
        when(serviceJobService.getJobByBookingId(1L)).thenReturn(jobResponseDto);

        ResponseEntity<ServiceJobResponseDto> response = serviceJobController.getJobByBookingId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().bookingId());
    }

    @Test
    void getJobByBookingId_NotFound() {
        when(serviceJobService.getJobByBookingId(999L))
                .thenThrow(new ResourceNotFoundException("Service job not found for booking ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.getJobByBookingId(999L));
    }

    @Test
    void getAllJobs_Success() {
        when(serviceJobService.getAllJobs(any()))
                .thenReturn(new PageImpl<>(List.of(jobResponseDto), PageRequest.of(0, 10), 1));

        ResponseEntity<Page<ServiceJobResponseDto>> response = serviceJobController.getAllJobs(0, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1L, response.getBody().getContent().get(0).id());
    }

    @Test
    void getAllJobs_WithCustomPagination() {
        when(serviceJobService.getAllJobs(any()))
                .thenReturn(new PageImpl<>(List.of(jobResponseDto), PageRequest.of(1, 5), 10));

        ResponseEntity<Page<ServiceJobResponseDto>> response = 
                serviceJobController.getAllJobs(1, 5, "jobCardNumber", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().getTotalElements());
        assertEquals(1L, response.getBody().getContent().get(0).id());
    }

    @Test
    void getJobsByVehicleId_Success() {
        when(serviceJobService.getJobsByVehicleId(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(jobResponseDto), PageRequest.of(0, 10), 1));

        ResponseEntity<Page<ServiceJobResponseDto>> response = 
                serviceJobController.getJobsByVehicleId(1L, 0, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1L, response.getBody().getContent().get(0).vehicleId());
    }

    @Test
    void getJobsByVehicleId_VehicleNotFound() {
        when(serviceJobService.getJobsByVehicleId(anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.getJobsByVehicleId(999L, 0, 10, "createdAt", "DESC"));
    }

    @Test
    void updateJobStatus_Success() {
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.INSPECTION, "Moving to inspection");
        ServiceJobResponseDto updatedJob = new ServiceJobResponseDto(
                1L, "JC-20261110-00001", 1L, 1L, "John Doe", "1234567890",
                1L, "ABC123", "Toyota", "Camry", JobStatus.INSPECTION,
                "Engine noise", "Initial inspection notes", "Technician notes",
                LocalDateTime.now(), LocalDateTime.now(), null, null, null, List.of()
        );

        when(serviceJobService.updateJobStatus(anyLong(), any(JobStatusUpdateDto.class)))
                .thenReturn(updatedJob);

        ResponseEntity<ServiceJobResponseDto> response = serviceJobController.updateJobStatus(1L, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(JobStatus.INSPECTION, response.getBody().jobStatus());
    }

    @Test
    void updateJobStatus_InvalidTransition() {
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.DELIVERED, "Invalid jump");

        when(serviceJobService.updateJobStatus(anyLong(), any(JobStatusUpdateDto.class)))
                .thenThrow(new IllegalStateException("Invalid status transition from CHECKED_IN to DELIVERED"));

        assertThrows(IllegalStateException.class,
                () -> serviceJobController.updateJobStatus(1L, updateDto));
    }

    @Test
    void updateJobNotes_Success() {
        JobNotesUpdateDto notesDto = new JobNotesUpdateDto(
                "Updated inspection notes", "Updated technician notes");

        when(serviceJobService.updateJobNotes(anyLong(), any(JobNotesUpdateDto.class)))
                .thenReturn(jobResponseDto);

        ResponseEntity<ServiceJobResponseDto> response = serviceJobController.updateJobNotes(1L, notesDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    void addInspection_Success() {
        JobInspectionRequestDto requestDto = new JobInspectionRequestDto(
                InspectionCategory.ENGINE, "Oil Level", InspectionCondition.GOOD, "Oil level is normal");
        JobInspectionResponseDto responseDto = new JobInspectionResponseDto(
                1L, 1L, InspectionCategory.ENGINE, "Oil Level",
                InspectionCondition.GOOD, "Oil level is normal", LocalDateTime.now());

        when(serviceJobService.addInspection(anyLong(), any(JobInspectionRequestDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<JobInspectionResponseDto> response = serviceJobController.addInspection(1L, requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(InspectionCategory.ENGINE, response.getBody().category());
        assertEquals("Oil Level", response.getBody().itemName());
        assertEquals(InspectionCondition.GOOD, response.getBody().condition());
    }

    @Test
    void addInspection_JobNotFound() {
        JobInspectionRequestDto requestDto = new JobInspectionRequestDto(
                InspectionCategory.ENGINE, "Test", InspectionCondition.GOOD, null);

        when(serviceJobService.addInspection(anyLong(), any(JobInspectionRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Service job not found with ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.addInspection(999L, requestDto));
    }

    @Test
    void getJobInspections_Success() {
        JobInspectionResponseDto inspectionDto = new JobInspectionResponseDto(
                1L, 1L, InspectionCategory.ENGINE, "Oil Level",
                InspectionCondition.GOOD, "Oil level is normal", LocalDateTime.now());

        when(serviceJobService.getJobInspections(1L)).thenReturn(List.of(inspectionDto));

        ResponseEntity<List<JobInspectionResponseDto>> response = serviceJobController.getJobInspections(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(InspectionCategory.ENGINE, response.getBody().get(0).category());
        assertEquals("Oil Level", response.getBody().get(0).itemName());
    }

    @Test
    void getJobInspections_JobNotFound() {
        when(serviceJobService.getJobInspections(999L))
                .thenThrow(new ResourceNotFoundException("Service job not found with ID: 999"));

        assertThrows(ResourceNotFoundException.class,
                () -> serviceJobController.getJobInspections(999L));
    }
}

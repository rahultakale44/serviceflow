package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.JobStatusUpdateDto;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceJobEstimateIntegrationTest {

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

    @Mock
    private RepairEstimateRepository repairEstimateRepository;

    @InjectMocks
    private ServiceJobService serviceJobService;

    private ServiceJob serviceJob;
    private RepairEstimate approvedEstimate;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .build();

        ServiceBooking booking = ServiceBooking.builder()
                .id(1L)
                .bookingNumber("BK-20261110-00001")
                .build();

        serviceJob = ServiceJob.builder()
                .id(1L)
                .jobCardNumber("JC-20261110-00001")
                .jobStatus(JobStatus.AWAITING_APPROVAL)
                .booking(booking)
                .customer(customer)
                .vehicle(vehicle)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        approvedEstimate = RepairEstimate.builder()
                .id(1L)
                .estimateNumber("EST-20261110-00001")
                .estimateStatus(EstimateStatus.APPROVED)
                .build();
    }

    @Test
    void updateJobStatus_AwaitingApprovalToApproved_WithApprovedEstimate() {
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.APPROVED, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(repairEstimateRepository.findLatestByJobIdAndStatus(1L, EstimateStatus.APPROVED))
                .thenReturn(Optional.of(approvedEstimate));
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L)).thenReturn(new ArrayList<>());
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);

        assertDoesNotThrow(() -> serviceJobService.updateJobStatus(1L, updateDto));
        
        verify(repairEstimateRepository, times(1))
                .findLatestByJobIdAndStatus(1L, EstimateStatus.APPROVED);
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_AwaitingApprovalToApproved_WithoutApprovedEstimate() {
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.APPROVED, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(repairEstimateRepository.findLatestByJobIdAndStatus(1L, EstimateStatus.APPROVED))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> serviceJobService.updateJobStatus(1L, updateDto));

        assertTrue(exception.getMessage().contains("approved repair estimate is required"));
        verify(repairEstimateRepository, times(1))
                .findLatestByJobIdAndStatus(1L, EstimateStatus.APPROVED);
        verify(serviceJobRepository, never()).save(any(ServiceJob.class));
    }

    @Test
    void updateJobStatus_OtherTransitions_NoApprovalCheckRequired() {
        serviceJob.setJobStatus(JobStatus.INSPECTION);
        JobStatusUpdateDto updateDto = new JobStatusUpdateDto(JobStatus.ESTIMATE_PENDING, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(1L)).thenReturn(new ArrayList<>());
        when(serviceJobRepository.save(any(ServiceJob.class))).thenReturn(serviceJob);

        assertDoesNotThrow(() -> serviceJobService.updateJobStatus(1L, updateDto));

        verify(repairEstimateRepository, never())
                .findLatestByJobIdAndStatus(anyLong(), any(EstimateStatus.class));
        verify(serviceJobRepository, times(1)).save(any(ServiceJob.class));
    }
}

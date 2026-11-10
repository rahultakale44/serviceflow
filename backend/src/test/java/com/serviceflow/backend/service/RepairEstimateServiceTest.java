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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairEstimateServiceTest {

    @Mock
    private RepairEstimateRepository repairEstimateRepository;

    @Mock
    private EstimateApprovalRepository estimateApprovalRepository;

    @Mock
    private ServiceJobRepository serviceJobRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EstimateCalculationService calculationService;

    @InjectMocks
    private RepairEstimateService repairEstimateService;

    private ServiceJob serviceJob;
    private Customer customer;
    private RepairEstimate estimate;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .build();

        serviceJob = ServiceJob.builder()
                .id(1L)
                .jobCardNumber("JC-20261110-00001")
                .jobStatus(JobStatus.ESTIMATE_PENDING)
                .customer(customer)
                .build();

        estimate = RepairEstimate.builder()
                .id(1L)
                .estimateNumber("EST-20261110-00001")
                .job(serviceJob)
                .version(1)
                .estimateStatus(EstimateStatus.DRAFT)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createDraftEstimate_Success() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Test notes");

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));
        when(repairEstimateRepository.findLatestVersionByJobId(1L)).thenReturn(Optional.empty());
        when(repairEstimateRepository.findAll()).thenReturn(new ArrayList<>());
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(estimate);

        RepairEstimateResponseDto response = repairEstimateService.createDraftEstimate(1L, requestDto);

        assertNotNull(response);
        verify(calculationService, times(1)).calculateEstimateTotals(any(RepairEstimate.class), isNull(), isNull());
        verify(repairEstimateRepository, times(1)).save(any(RepairEstimate.class));
    }

    @Test
    void createDraftEstimate_JobNotFound() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, null);

        when(serviceJobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> repairEstimateService.createDraftEstimate(999L, requestDto));
    }

    @Test
    void createDraftEstimate_InvalidJobStatus() {
        serviceJob.setJobStatus(JobStatus.DELIVERED);
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repairEstimateService.createDraftEstimate(1L, requestDto));

        assertTrue(exception.getMessage().contains("INSPECTION or ESTIMATE_PENDING"));
    }

    @Test
    void createDraftEstimate_EmptyItems() {
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                new ArrayList<>(), null, null, null);

        when(serviceJobRepository.findById(1L)).thenReturn(Optional.of(serviceJob));

        assertThrows(IllegalArgumentException.class,
                () -> repairEstimateService.createDraftEstimate(1L, requestDto));
    }

    @Test
    void updateDraftEstimate_Success() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Updated Oil Change", EstimateItemType.LABOUR, 2, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Updated notes");

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(estimate);

        RepairEstimateResponseDto response = repairEstimateService.updateDraftEstimate(1L, 1L, requestDto);

        assertNotNull(response);
        verify(calculationService, times(1)).calculateEstimateTotals(any(RepairEstimate.class), isNull(), isNull());
        verify(repairEstimateRepository, times(1)).save(any(RepairEstimate.class));
    }

    @Test
    void updateDraftEstimate_NotDraftStatus() {
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, null);

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repairEstimateService.updateDraftEstimate(1L, 1L, requestDto));

        assertTrue(exception.getMessage().contains("Only DRAFT"));
    }

    @Test
    void submitForApproval_Success() {
        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(estimate);

        RepairEstimateResponseDto response = repairEstimateService.submitForApproval(1L, 1L);

        assertNotNull(response);
        verify(repairEstimateRepository, times(1)).save(any(RepairEstimate.class));
    }

    @Test
    void submitForApproval_NotDraftStatus() {
        estimate.setEstimateStatus(EstimateStatus.APPROVED);
        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repairEstimateService.submitForApproval(1L, 1L));

        assertTrue(exception.getMessage().contains("Only DRAFT"));
    }

    @Test
    void approveEstimate_Success() {
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);
        EstimateApprovalRequestDto requestDto = new EstimateApprovalRequestDto(1L, "Looks good");

        EstimateApproval approval = EstimateApproval.builder()
                .id(1L)
                .estimate(estimate)
                .customer(customer)
                .decision(ApprovalDecision.APPROVED)
                .decisionTimestamp(LocalDateTime.now())
                .build();

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(estimateApprovalRepository.existsByEstimateId(1L)).thenReturn(false);
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(estimate);
        when(estimateApprovalRepository.save(any(EstimateApproval.class))).thenReturn(approval);

        EstimateApprovalResponseDto response = repairEstimateService.approveEstimate(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(ApprovalDecision.APPROVED, response.decision());
        verify(estimateApprovalRepository, times(1)).save(any(EstimateApproval.class));
    }

    @Test
    void approveEstimate_NotPendingApproval() {
        estimate.setEstimateStatus(EstimateStatus.DRAFT);
        EstimateApprovalRequestDto requestDto = new EstimateApprovalRequestDto(1L, null);

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repairEstimateService.approveEstimate(1L, 1L, requestDto));

        assertTrue(exception.getMessage().contains("Only PENDING_APPROVAL"));
    }

    @Test
    void approveEstimate_DuplicateApproval() {
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);
        EstimateApprovalRequestDto requestDto = new EstimateApprovalRequestDto(1L, null);

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(estimateApprovalRepository.existsByEstimateId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> repairEstimateService.approveEstimate(1L, 1L, requestDto));
    }

    @Test
    void rejectEstimate_Success() {
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);
        EstimateRejectionRequestDto requestDto = new EstimateRejectionRequestDto(
                1L, "Too expensive", "Please provide cheaper options");

        EstimateApproval approval = EstimateApproval.builder()
                .id(1L)
                .estimate(estimate)
                .customer(customer)
                .decision(ApprovalDecision.REJECTED)
                .rejectionReason("Too expensive")
                .decisionTimestamp(LocalDateTime.now())
                .build();

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(estimateApprovalRepository.existsByEstimateId(1L)).thenReturn(false);
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(estimate);
        when(estimateApprovalRepository.save(any(EstimateApproval.class))).thenReturn(approval);

        EstimateApprovalResponseDto response = repairEstimateService.rejectEstimate(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(ApprovalDecision.REJECTED, response.decision());
        assertEquals("Too expensive", response.rejectionReason());
        verify(estimateApprovalRepository, times(1)).save(any(EstimateApproval.class));
    }

    @Test
    void reviseEstimate_Success() {
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Revised Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("60.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Revised estimate");

        RepairEstimate newEstimate = RepairEstimate.builder()
                .id(2L)
                .estimateNumber("EST-20261110-00002")
                .job(serviceJob)
                .version(2)
                .estimateStatus(EstimateStatus.DRAFT)
                .build();

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));
        when(repairEstimateRepository.save(any(RepairEstimate.class))).thenReturn(newEstimate);
        when(repairEstimateRepository.findAll()).thenReturn(new ArrayList<>());

        RepairEstimateResponseDto response = repairEstimateService.reviseEstimate(1L, 1L, requestDto);

        assertNotNull(response);
        verify(repairEstimateRepository, times(2)).save(any(RepairEstimate.class));
    }

    @Test
    void reviseEstimate_NotPendingApproval() {
        estimate.setEstimateStatus(EstimateStatus.APPROVED);
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, null);

        when(repairEstimateRepository.findById(1L)).thenReturn(Optional.of(estimate));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repairEstimateService.reviseEstimate(1L, 1L, requestDto));

        assertTrue(exception.getMessage().contains("Only PENDING_APPROVAL"));
    }
}

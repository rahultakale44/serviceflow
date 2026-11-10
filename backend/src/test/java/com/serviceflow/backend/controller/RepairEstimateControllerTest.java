package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.ApprovalDecision;
import com.serviceflow.backend.entity.EstimateItemType;
import com.serviceflow.backend.entity.EstimateStatus;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.RepairEstimateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairEstimateControllerTest {

    @Mock
    private RepairEstimateService repairEstimateService;

    @InjectMocks
    private RepairEstimateController repairEstimateController;

    private RepairEstimateResponseDto estimateResponseDto;
    private EstimateApprovalResponseDto approvalResponseDto;

    @BeforeEach
    void setUp() {
        RepairEstimateItemResponseDto itemDto = new RepairEstimateItemResponseDto(
                1L, "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"), new BigDecimal("50.00"));

        estimateResponseDto = new RepairEstimateResponseDto(
                1L, "EST-20261110-00001", 1L, "JC-20261110-00001", 1, EstimateStatus.DRAFT,
                new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"),
                "Test notes", LocalDateTime.now(), LocalDateTime.now(), null, null, List.of(itemDto));

        approvalResponseDto = new EstimateApprovalResponseDto(
                1L, 1L, "EST-20261110-00001", 1L, "John Doe", ApprovalDecision.APPROVED,
                LocalDateTime.now(), null, "Looks good");
    }

    @Test
    void createDraftEstimate_Success() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Test notes");

        when(repairEstimateService.createDraftEstimate(anyLong(), any(RepairEstimateRequestDto.class)))
                .thenReturn(estimateResponseDto);

        ResponseEntity<RepairEstimateResponseDto> response = repairEstimateController.createDraftEstimate(1L, requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EST-20261110-00001", response.getBody().estimateNumber());
        verify(repairEstimateService, times(1)).createDraftEstimate(anyLong(), any(RepairEstimateRequestDto.class));
    }

    @Test
    void createDraftEstimate_JobNotFound() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, null);

        when(repairEstimateService.createDraftEstimate(anyLong(), any(RepairEstimateRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Service job not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> repairEstimateController.createDraftEstimate(999L, requestDto));
    }

    @Test
    void getAllEstimatesForJob_Success() {
        when(repairEstimateService.getAllEstimatesForJob(1L)).thenReturn(List.of(estimateResponseDto));

        ResponseEntity<List<RepairEstimateResponseDto>> response = repairEstimateController.getAllEstimatesForJob(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getEstimateById_Success() {
        when(repairEstimateService.getEstimateById(1L, 1L)).thenReturn(estimateResponseDto);

        ResponseEntity<RepairEstimateResponseDto> response = repairEstimateController.getEstimateById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    void updateDraftEstimate_Success() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Updated Oil Change", EstimateItemType.LABOUR, 2, new BigDecimal("50.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Updated notes");

        when(repairEstimateService.updateDraftEstimate(anyLong(), anyLong(), any(RepairEstimateRequestDto.class)))
                .thenReturn(estimateResponseDto);

        ResponseEntity<RepairEstimateResponseDto> response = 
                repairEstimateController.updateDraftEstimate(1L, 1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void submitForApproval_Success() {
        estimateResponseDto = new RepairEstimateResponseDto(
                1L, "EST-20261110-00001", 1L, "JC-20261110-00001", 1, EstimateStatus.PENDING_APPROVAL,
                new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"),
                "Test notes", LocalDateTime.now(), LocalDateTime.now(), null, null, List.of());

        when(repairEstimateService.submitForApproval(1L, 1L)).thenReturn(estimateResponseDto);

        ResponseEntity<RepairEstimateResponseDto> response = repairEstimateController.submitForApproval(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(EstimateStatus.PENDING_APPROVAL, response.getBody().estimateStatus());
    }

    @Test
    void approveEstimate_Success() {
        EstimateApprovalRequestDto requestDto = new EstimateApprovalRequestDto(1L, "Looks good");

        when(repairEstimateService.approveEstimate(anyLong(), anyLong(), any(EstimateApprovalRequestDto.class)))
                .thenReturn(approvalResponseDto);

        ResponseEntity<EstimateApprovalResponseDto> response = 
                repairEstimateController.approveEstimate(1L, 1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApprovalDecision.APPROVED, response.getBody().decision());
    }

    @Test
    void rejectEstimate_Success() {
        EstimateRejectionRequestDto requestDto = new EstimateRejectionRequestDto(
                1L, "Too expensive", "Please revise");

        EstimateApprovalResponseDto rejectionResponse = new EstimateApprovalResponseDto(
                1L, 1L, "EST-20261110-00001", 1L, "John Doe", ApprovalDecision.REJECTED,
                LocalDateTime.now(), "Too expensive", "Please revise");

        when(repairEstimateService.rejectEstimate(anyLong(), anyLong(), any(EstimateRejectionRequestDto.class)))
                .thenReturn(rejectionResponse);

        ResponseEntity<EstimateApprovalResponseDto> response = 
                repairEstimateController.rejectEstimate(1L, 1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApprovalDecision.REJECTED, response.getBody().decision());
        assertEquals("Too expensive", response.getBody().rejectionReason());
    }

    @Test
    void reviseEstimate_Success() {
        RepairEstimateItemRequestDto itemDto = new RepairEstimateItemRequestDto(
                "Revised Oil Change", EstimateItemType.LABOUR, 1, new BigDecimal("60.00"));
        RepairEstimateRequestDto requestDto = new RepairEstimateRequestDto(
                List.of(itemDto), null, null, "Revised estimate");

        RepairEstimateResponseDto revisedDto = new RepairEstimateResponseDto(
                2L, "EST-20261110-00002", 1L, "JC-20261110-00001", 2, EstimateStatus.DRAFT,
                new BigDecimal("60.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("60.00"),
                "Revised estimate", LocalDateTime.now(), LocalDateTime.now(), null, null, List.of());

        when(repairEstimateService.reviseEstimate(anyLong(), anyLong(), any(RepairEstimateRequestDto.class)))
                .thenReturn(revisedDto);

        ResponseEntity<RepairEstimateResponseDto> response = 
                repairEstimateController.reviseEstimate(1L, 1L, requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().version());
    }
}

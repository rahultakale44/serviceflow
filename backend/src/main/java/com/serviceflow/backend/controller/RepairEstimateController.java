package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.service.RepairEstimateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for repair estimate management.
 * 
 * Base path: /api/v1/jobs/{jobId}/estimates
 * 
 * IMPORTANT SECURITY NOTE:
 * This implementation uses temporary development security (permitAll).
 * Customer approval endpoints do not have production-ready authentication.
 * The customerId parameter in approval/rejection requests is NOT verified
 * against an authenticated session token. This allows estimate workflow
 * testing without JWT infrastructure.
 * 
 * BEFORE PRODUCTION:
 * - Implement JWT-based customer authentication
 * - Extract customerId from authenticated token, not from request body
 * - Add role-based authorization (customer vs. manager)
 * - Validate customer identity matches job owner
 */
@RestController
@RequestMapping("/api/v1/jobs/{jobId}/estimates")
public class RepairEstimateController {

    private final RepairEstimateService repairEstimateService;

    public RepairEstimateController(RepairEstimateService repairEstimateService) {
        this.repairEstimateService = repairEstimateService;
    }

    /**
     * Create a draft estimate for a job.
     * 
     * @param jobId the job ID
     * @param requestDto estimate creation request
     * @return created estimate (201 Created)
     */
    @PostMapping
    public ResponseEntity<RepairEstimateResponseDto> createDraftEstimate(
            @PathVariable Long jobId,
            @Valid @RequestBody RepairEstimateRequestDto requestDto) {
        RepairEstimateResponseDto response = repairEstimateService.createDraftEstimate(jobId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all estimate versions for a job.
     * 
     * @param jobId the job ID
     * @return list of estimates ordered by version descending (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<RepairEstimateResponseDto>> getAllEstimatesForJob(@PathVariable Long jobId) {
        List<RepairEstimateResponseDto> response = repairEstimateService.getAllEstimatesForJob(jobId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific estimate by ID.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID
     * @return estimate details (200 OK)
     */
    @GetMapping("/{estimateId}")
    public ResponseEntity<RepairEstimateResponseDto> getEstimateById(
            @PathVariable Long jobId,
            @PathVariable Long estimateId) {
        RepairEstimateResponseDto response = repairEstimateService.getEstimateById(jobId, estimateId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a draft estimate.
     * Only DRAFT estimates can be updated.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID
     * @param requestDto estimate update request
     * @return updated estimate (200 OK)
     */
    @PutMapping("/{estimateId}")
    public ResponseEntity<RepairEstimateResponseDto> updateDraftEstimate(
            @PathVariable Long jobId,
            @PathVariable Long estimateId,
            @Valid @RequestBody RepairEstimateRequestDto requestDto) {
        RepairEstimateResponseDto response = repairEstimateService.updateDraftEstimate(jobId, estimateId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Submit estimate for customer approval.
     * Changes status from DRAFT to PENDING_APPROVAL.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID
     * @return updated estimate (200 OK)
     */
    @PostMapping("/{estimateId}/submit")
    public ResponseEntity<RepairEstimateResponseDto> submitForApproval(
            @PathVariable Long jobId,
            @PathVariable Long estimateId) {
        RepairEstimateResponseDto response = repairEstimateService.submitForApproval(jobId, estimateId);
        return ResponseEntity.ok(response);
    }

    /**
     * Record customer approval of an estimate.
     * 
     * SECURITY WARNING: customerId comes from request body in development mode.
     * In production, extract customerId from authenticated JWT token.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID
     * @param requestDto approval request with customer ID
     * @return approval record (200 OK)
     */
    @PostMapping("/{estimateId}/approve")
    public ResponseEntity<EstimateApprovalResponseDto> approveEstimate(
            @PathVariable Long jobId,
            @PathVariable Long estimateId,
            @Valid @RequestBody EstimateApprovalRequestDto requestDto) {
        EstimateApprovalResponseDto response = repairEstimateService.approveEstimate(jobId, estimateId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Record customer rejection of an estimate.
     * 
     * SECURITY WARNING: customerId comes from request body in development mode.
     * In production, extract customerId from authenticated JWT token.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID
     * @param requestDto rejection request with customer ID and reason
     * @return approval record with rejection (200 OK)
     */
    @PostMapping("/{estimateId}/reject")
    public ResponseEntity<EstimateApprovalResponseDto> rejectEstimate(
            @PathVariable Long jobId,
            @PathVariable Long estimateId,
            @Valid @RequestBody EstimateRejectionRequestDto requestDto) {
        EstimateApprovalResponseDto response = repairEstimateService.rejectEstimate(jobId, estimateId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new revision of an estimate.
     * The old estimate is marked as SUPERSEDED and a new DRAFT is created with incremented version.
     * 
     * @param jobId the job ID
     * @param estimateId the estimate ID to revise
     * @param requestDto new estimate content
     * @return new estimate revision (201 Created)
     */
    @PostMapping("/{estimateId}/revise")
    public ResponseEntity<RepairEstimateResponseDto> reviseEstimate(
            @PathVariable Long jobId,
            @PathVariable Long estimateId,
            @Valid @RequestBody RepairEstimateRequestDto requestDto) {
        RepairEstimateResponseDto response = repairEstimateService.reviseEstimate(jobId, estimateId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

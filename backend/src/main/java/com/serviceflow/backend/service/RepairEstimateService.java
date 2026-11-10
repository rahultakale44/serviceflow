package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepairEstimateService {

    private final RepairEstimateRepository repairEstimateRepository;
    private final EstimateApprovalRepository estimateApprovalRepository;
    private final ServiceJobRepository serviceJobRepository;
    private final CustomerRepository customerRepository;
    private final EstimateCalculationService calculationService;

    public RepairEstimateService(RepairEstimateRepository repairEstimateRepository,
                                 EstimateApprovalRepository estimateApprovalRepository,
                                 ServiceJobRepository serviceJobRepository,
                                 CustomerRepository customerRepository,
                                 EstimateCalculationService calculationService) {
        this.repairEstimateRepository = repairEstimateRepository;
        this.estimateApprovalRepository = estimateApprovalRepository;
        this.serviceJobRepository = serviceJobRepository;
        this.customerRepository = customerRepository;
        this.calculationService = calculationService;
    }

    @Transactional
    public RepairEstimateResponseDto createDraftEstimate(Long jobId, RepairEstimateRequestDto requestDto) {
        // Validate job exists
        ServiceJob job = serviceJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found with ID: " + jobId));

        // Validate job is in ESTIMATE_PENDING or INSPECTION status
        if (job.getJobStatus() != JobStatus.ESTIMATE_PENDING && job.getJobStatus() != JobStatus.INSPECTION) {
            throw new IllegalStateException(
                    "Can only create estimate for jobs in INSPECTION or ESTIMATE_PENDING status. Current status: " 
                            + job.getJobStatus());
        }

        // Validate at least one item
        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            throw new IllegalArgumentException("At least one estimate item is required");
        }

        // Determine next version number
        Integer nextVersion = determineNextVersion(jobId);

        // Generate estimate number
        String estimateNumber = generateEstimateNumber();

        // Create estimate entity
        RepairEstimate estimate = RepairEstimate.builder()
                .estimateNumber(estimateNumber)
                .job(job)
                .version(nextVersion)
                .estimateStatus(EstimateStatus.DRAFT)
                .notes(requestDto.notes())
                .build();

        // Add line items
        requestDto.items().forEach(itemDto -> {
            RepairEstimateItem item = RepairEstimateItem.builder()
                    .description(itemDto.description())
                    .itemType(itemDto.itemType())
                    .quantity(itemDto.quantity())
                    .unitPrice(itemDto.unitPrice())
                    .build();
            estimate.addItem(item);
        });

        // Calculate totals
        calculationService.calculateEstimateTotals(estimate, requestDto.taxRatePercentage(), requestDto.discountAmount());

        // Save estimate
        RepairEstimate savedEstimate = repairEstimateRepository.save(estimate);

        return mapToResponseDto(savedEstimate);
    }

    @Transactional(readOnly = true)
    public RepairEstimateResponseDto getEstimateById(Long jobId, Long estimateId) {
        RepairEstimate estimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!estimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        return mapToResponseDto(estimate);
    }

    @Transactional(readOnly = true)
    public List<RepairEstimateResponseDto> getAllEstimatesForJob(Long jobId) {
        // Validate job exists
        if (!serviceJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Service job not found with ID: " + jobId);
        }

        return repairEstimateRepository.findByJobIdOrderByVersionDesc(jobId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RepairEstimateResponseDto updateDraftEstimate(Long jobId, Long estimateId, RepairEstimateRequestDto requestDto) {
        RepairEstimate estimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!estimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        // Only DRAFT estimates can be updated
        if (estimate.getEstimateStatus() != EstimateStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT estimates can be updated. Current status: " + estimate.getEstimateStatus());
        }

        // Validate at least one item
        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            throw new IllegalArgumentException("At least one estimate item is required");
        }

        // Clear existing items and add new ones
        estimate.clearItems();

        requestDto.items().forEach(itemDto -> {
            RepairEstimateItem item = RepairEstimateItem.builder()
                    .description(itemDto.description())
                    .itemType(itemDto.itemType())
                    .quantity(itemDto.quantity())
                    .unitPrice(itemDto.unitPrice())
                    .build();
            estimate.addItem(item);
        });

        // Update notes
        estimate.setNotes(requestDto.notes());

        // Recalculate totals
        calculationService.calculateEstimateTotals(estimate, requestDto.taxRatePercentage(), requestDto.discountAmount());

        // Save estimate
        RepairEstimate updatedEstimate = repairEstimateRepository.save(estimate);

        return mapToResponseDto(updatedEstimate);
    }

    @Transactional
    public RepairEstimateResponseDto submitForApproval(Long jobId, Long estimateId) {
        RepairEstimate estimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!estimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        // Only DRAFT estimates can be submitted
        if (estimate.getEstimateStatus() != EstimateStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT estimates can be submitted for approval. Current status: " + estimate.getEstimateStatus());
        }

        // Update status to PENDING_APPROVAL
        estimate.setEstimateStatus(EstimateStatus.PENDING_APPROVAL);

        RepairEstimate savedEstimate = repairEstimateRepository.save(estimate);

        return mapToResponseDto(savedEstimate);
    }

    @Transactional
    public EstimateApprovalResponseDto approveEstimate(Long jobId, Long estimateId, EstimateApprovalRequestDto requestDto) {
        RepairEstimate estimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!estimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + requestDto.customerId()));

        // Validate customer matches job customer
        if (!estimate.getJob().getCustomer().getId().equals(requestDto.customerId())) {
            throw new IllegalArgumentException("Customer does not match the job's customer");
        }

        // Only PENDING_APPROVAL estimates can be approved
        if (estimate.getEstimateStatus() != EstimateStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Only PENDING_APPROVAL estimates can be approved. Current status: " + estimate.getEstimateStatus());
        }

        // Check for duplicate approval
        if (estimateApprovalRepository.existsByEstimateId(estimateId)) {
            throw new DuplicateResourceException("This estimate has already been approved or rejected");
        }

        // Update estimate status
        estimate.setEstimateStatus(EstimateStatus.APPROVED);
        estimate.setApprovedAt(LocalDateTime.now());

        // Create approval record
        EstimateApproval approval = EstimateApproval.builder()
                .estimate(estimate)
                .customer(customer)
                .decision(ApprovalDecision.APPROVED)
                .customerComments(requestDto.customerComments())
                .build();

        repairEstimateRepository.save(estimate);
        EstimateApproval savedApproval = estimateApprovalRepository.save(approval);

        return mapToApprovalResponseDto(savedApproval);
    }

    @Transactional
    public EstimateApprovalResponseDto rejectEstimate(Long jobId, Long estimateId, EstimateRejectionRequestDto requestDto) {
        RepairEstimate estimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!estimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + requestDto.customerId()));

        // Validate customer matches job customer
        if (!estimate.getJob().getCustomer().getId().equals(requestDto.customerId())) {
            throw new IllegalArgumentException("Customer does not match the job's customer");
        }

        // Only PENDING_APPROVAL estimates can be rejected
        if (estimate.getEstimateStatus() != EstimateStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Only PENDING_APPROVAL estimates can be rejected. Current status: " + estimate.getEstimateStatus());
        }

        // Check for duplicate decision
        if (estimateApprovalRepository.existsByEstimateId(estimateId)) {
            throw new DuplicateResourceException("This estimate has already been approved or rejected");
        }

        // Update estimate status
        estimate.setEstimateStatus(EstimateStatus.REJECTED);
        estimate.setRejectedAt(LocalDateTime.now());

        // Create approval record with rejection
        EstimateApproval approval = EstimateApproval.builder()
                .estimate(estimate)
                .customer(customer)
                .decision(ApprovalDecision.REJECTED)
                .rejectionReason(requestDto.rejectionReason())
                .customerComments(requestDto.customerComments())
                .build();

        repairEstimateRepository.save(estimate);
        EstimateApproval savedApproval = estimateApprovalRepository.save(approval);

        return mapToApprovalResponseDto(savedApproval);
    }

    @Transactional
    public RepairEstimateResponseDto reviseEstimate(Long jobId, Long estimateId, RepairEstimateRequestDto requestDto) {
        RepairEstimate oldEstimate = repairEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair estimate not found with ID: " + estimateId));

        // Validate estimate belongs to the specified job
        if (!oldEstimate.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException("Estimate does not belong to the specified job");
        }

        // Only PENDING_APPROVAL estimates can be revised
        if (oldEstimate.getEstimateStatus() != EstimateStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Only PENDING_APPROVAL estimates can be revised. Current status: " + oldEstimate.getEstimateStatus());
        }

        // Mark old estimate as SUPERSEDED
        oldEstimate.setEstimateStatus(EstimateStatus.SUPERSEDED);
        repairEstimateRepository.save(oldEstimate);

        // Create new estimate as revision
        Integer nextVersion = oldEstimate.getVersion() + 1;
        String estimateNumber = generateEstimateNumber();

        RepairEstimate newEstimate = RepairEstimate.builder()
                .estimateNumber(estimateNumber)
                .job(oldEstimate.getJob())
                .version(nextVersion)
                .estimateStatus(EstimateStatus.DRAFT)
                .notes(requestDto.notes())
                .build();

        // Add line items
        requestDto.items().forEach(itemDto -> {
            RepairEstimateItem item = RepairEstimateItem.builder()
                    .description(itemDto.description())
                    .itemType(itemDto.itemType())
                    .quantity(itemDto.quantity())
                    .unitPrice(itemDto.unitPrice())
                    .build();
            newEstimate.addItem(item);
        });

        // Calculate totals
        calculationService.calculateEstimateTotals(newEstimate, requestDto.taxRatePercentage(), requestDto.discountAmount());

        // Save new estimate
        RepairEstimate savedEstimate = repairEstimateRepository.save(newEstimate);

        return mapToResponseDto(savedEstimate);
    }

    private Integer determineNextVersion(Long jobId) {
        return repairEstimateRepository.findLatestVersionByJobId(jobId)
                .map(estimate -> estimate.getVersion() + 1)
                .orElse(1);
    }

    @Transactional
    private synchronized String generateEstimateNumber() {
        LocalDate today = LocalDate.now();
        String datePrefix = "EST-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";

        String lastEstimateNumber = repairEstimateRepository.findAll().stream()
                .map(RepairEstimate::getEstimateNumber)
                .filter(num -> num.startsWith(datePrefix))
                .max(String::compareTo)
                .orElse(null);

        int nextSequence = 1;
        if (lastEstimateNumber != null) {
            String sequencePart = lastEstimateNumber.substring(datePrefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        return datePrefix + String.format("%05d", nextSequence);
    }

    private RepairEstimateResponseDto mapToResponseDto(RepairEstimate estimate) {
        List<RepairEstimateItemResponseDto> itemDtos = estimate.getItems().stream()
                .map(item -> new RepairEstimateItemResponseDto(
                        item.getId(),
                        item.getDescription(),
                        item.getItemType(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .collect(Collectors.toList());

        return new RepairEstimateResponseDto(
                estimate.getId(),
                estimate.getEstimateNumber(),
                estimate.getJob().getId(),
                estimate.getJob().getJobCardNumber(),
                estimate.getVersion(),
                estimate.getEstimateStatus(),
                estimate.getSubtotal(),
                estimate.getTaxAmount(),
                estimate.getDiscountAmount(),
                estimate.getTotalAmount(),
                estimate.getNotes(),
                estimate.getCreatedAt(),
                estimate.getUpdatedAt(),
                estimate.getApprovedAt(),
                estimate.getRejectedAt(),
                itemDtos
        );
    }

    private EstimateApprovalResponseDto mapToApprovalResponseDto(EstimateApproval approval) {
        return new EstimateApprovalResponseDto(
                approval.getId(),
                approval.getEstimate().getId(),
                approval.getEstimate().getEstimateNumber(),
                approval.getCustomer().getId(),
                approval.getCustomer().getFirstName() + " " + approval.getCustomer().getLastName(),
                approval.getDecision(),
                approval.getDecisionTimestamp(),
                approval.getRejectionReason(),
                approval.getCustomerComments()
        );
    }
}

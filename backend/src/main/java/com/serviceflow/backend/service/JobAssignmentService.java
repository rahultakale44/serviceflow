package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.JobAssignmentRequestDto;
import com.serviceflow.backend.dto.JobAssignmentResponseDto;
import com.serviceflow.backend.dto.ReassignmentRequestDto;
import com.serviceflow.backend.entity.*;
import com.serviceflow.backend.exception.AssignmentConflictException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for job assignment operations.
 * Implements assignment business rules, conflict detection, and scheduling validation.
 * 
 * Assignment lifecycle is kept separate from JobStatus lifecycle.
 * This service does not automatically update job status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobAssignmentService {

    private final JobAssignmentRepository assignmentRepository;
    private final ServiceJobRepository serviceJobRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceBayRepository serviceBayRepository;
    private final RepairEstimateRepository repairEstimateRepository;

    /**
     * Create a new job assignment.
     * Validates all business rules and checks for scheduling conflicts.
     */
    @Transactional
    public JobAssignmentResponseDto createAssignment(JobAssignmentRequestDto requestDto) {
        log.info("Creating assignment for job id: {}", requestDto.getJobId());

        // Validate schedule times
        validateScheduleTimes(requestDto.getScheduledStart(), requestDto.getScheduledEnd());

        // Validate job exists
        ServiceJob job = serviceJobRepository.findById(requestDto.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Job", requestDto.getJobId()));

        // Validate technician exists and is active
        Technician technician = technicianRepository.findById(requestDto.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician", requestDto.getTechnicianId()));

        if (!technician.getActive()) {
            throw new IllegalArgumentException("Cannot assign inactive technician: " + technician.getTechnicianCode());
        }

        // Validate bay exists and is active
        ServiceBay bay = serviceBayRepository.findById(requestDto.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Bay", requestDto.getBayId()));

        if (!bay.getActive()) {
            throw new IllegalArgumentException("Cannot assign inactive service bay: " + bay.getBayCode());
        }

        // Validate estimate approval if job requires approval
        validateEstimateApproval(job);

        // Check for scheduling conflicts
        checkSchedulingConflicts(
                requestDto.getJobId(),
                requestDto.getTechnicianId(),
                requestDto.getBayId(),
                requestDto.getScheduledStart(),
                requestDto.getScheduledEnd(),
                null // No assignment ID to exclude for new assignments
        );

        // Create assignment
        JobAssignment assignment = JobAssignment.builder()
                .job(job)
                .technician(technician)
                .bay(bay)
                .scheduledStart(requestDto.getScheduledStart())
                .scheduledEnd(requestDto.getScheduledEnd())
                .notes(requestDto.getNotes())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        JobAssignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment created successfully with id: {}", savedAssignment.getId());

        return mapToResponseDto(savedAssignment);
    }

    /**
     * Get assignment by ID.
     */
    @Transactional(readOnly = true)
    public JobAssignmentResponseDto getAssignmentById(Long id) {
        log.info("Fetching assignment with id: {}", id);
        JobAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Assignment", id));
        return mapToResponseDto(assignment);
    }

    /**
     * Get all assignments with pagination.
     */
    @Transactional(readOnly = true)
    public Page<JobAssignmentResponseDto> getAllAssignments(Pageable pageable) {
        log.info("Fetching assignments with pagination: {}", pageable);
        return assignmentRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get assignments for a specific job.
     */
    @Transactional(readOnly = true)
    public Page<JobAssignmentResponseDto> getAssignmentsByJob(Long jobId, Pageable pageable) {
        log.info("Fetching assignments for job id: {}", jobId);
        
        // Validate job exists
        if (!serviceJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Service Job", jobId);
        }

        return assignmentRepository.findByJobId(jobId, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get assignments for a specific technician.
     */
    @Transactional(readOnly = true)
    public Page<JobAssignmentResponseDto> getAssignmentsByTechnician(Long technicianId, Pageable pageable) {
        log.info("Fetching assignments for technician id: {}", technicianId);

        // Validate technician exists
        if (!technicianRepository.existsById(technicianId)) {
            throw new ResourceNotFoundException("Technician", technicianId);
        }

        return assignmentRepository.findByTechnicianId(technicianId, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Complete an assignment.
     * Sets status to COMPLETED and records completion timestamp.
     */
    @Transactional
    public JobAssignmentResponseDto completeAssignment(Long id) {
        log.info("Completing assignment with id: {}", id);

        JobAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Assignment", id));

        if (assignment.getAssignmentStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Can only complete ACTIVE assignments. Current status: " + assignment.getAssignmentStatus());
        }

        assignment.setAssignmentStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());

        JobAssignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment completed successfully");

        return mapToResponseDto(updatedAssignment);
    }

    /**
     * Cancel an assignment.
     * Sets status to CANCELLED and records cancellation timestamp.
     * Assignment history is preserved.
     */
    @Transactional
    public JobAssignmentResponseDto cancelAssignment(Long id) {
        log.info("Cancelling assignment with id: {}", id);

        JobAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Assignment", id));

        if (assignment.getAssignmentStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Can only cancel ACTIVE assignments. Current status: " + assignment.getAssignmentStatus());
        }

        assignment.setAssignmentStatus(AssignmentStatus.CANCELLED);
        assignment.setCancelledAt(LocalDateTime.now());

        JobAssignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment cancelled successfully");

        return mapToResponseDto(updatedAssignment);
    }

    /**
     * Reassign a job to a different technician or bay.
     * Cancels the old assignment and creates a new active assignment.
     * Preserves complete assignment history.
     */
    @Transactional
    public JobAssignmentResponseDto reassignJob(Long assignmentId, ReassignmentRequestDto requestDto) {
        log.info("Reassigning job from assignment id: {}", assignmentId);

        // Validate schedule times
        validateScheduleTimes(requestDto.getScheduledStart(), requestDto.getScheduledEnd());

        // Get existing assignment
        JobAssignment oldAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Assignment", assignmentId));

        if (oldAssignment.getAssignmentStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Can only reassign ACTIVE assignments. Current status: " + oldAssignment.getAssignmentStatus());
        }

        // Validate new technician exists and is active
        Technician newTechnician = technicianRepository.findById(requestDto.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician", requestDto.getTechnicianId()));

        if (!newTechnician.getActive()) {
            throw new IllegalArgumentException("Cannot assign inactive technician: " + newTechnician.getTechnicianCode());
        }

        // Validate new bay exists and is active
        ServiceBay newBay = serviceBayRepository.findById(requestDto.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Bay", requestDto.getBayId()));

        if (!newBay.getActive()) {
            throw new IllegalArgumentException("Cannot assign inactive service bay: " + newBay.getBayCode());
        }

        // Check for scheduling conflicts (excluding the current assignment)
        checkSchedulingConflicts(
                oldAssignment.getJob().getId(),
                requestDto.getTechnicianId(),
                requestDto.getBayId(),
                requestDto.getScheduledStart(),
                requestDto.getScheduledEnd(),
                assignmentId // Exclude current assignment from conflict check
        );

        // Cancel old assignment
        oldAssignment.setAssignmentStatus(AssignmentStatus.CANCELLED);
        oldAssignment.setCancelledAt(LocalDateTime.now());
        assignmentRepository.save(oldAssignment);

        // Create new assignment
        JobAssignment newAssignment = JobAssignment.builder()
                .job(oldAssignment.getJob())
                .technician(newTechnician)
                .bay(newBay)
                .scheduledStart(requestDto.getScheduledStart())
                .scheduledEnd(requestDto.getScheduledEnd())
                .notes(requestDto.getNotes())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        JobAssignment savedAssignment = assignmentRepository.save(newAssignment);
        log.info("Job reassigned successfully. New assignment id: {}", savedAssignment.getId());

        return mapToResponseDto(savedAssignment);
    }

    /**
     * Validate that scheduled end is after scheduled start.
     */
    private void validateScheduleTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Scheduled end must be after scheduled start");
        }
    }

    /**
     * Validate that job has an approved estimate if required.
     * Jobs should not be assigned for repair work without estimate approval.
     */
    private void validateEstimateApproval(ServiceJob job) {
        JobStatus jobStatus = job.getJobStatus();

        // If job is in states requiring estimate approval, verify it exists
        if (jobStatus == JobStatus.APPROVED || 
            jobStatus == JobStatus.IN_SERVICE || 
            jobStatus == JobStatus.QUALITY_CHECK || 
            jobStatus == JobStatus.READY_FOR_DELIVERY) {
            
            boolean hasApprovedEstimate = repairEstimateRepository
                    .findCurrentEstimateByJobId(job.getId())
                    .map(estimate -> estimate.getEstimateStatus() == EstimateStatus.APPROVED)
                    .orElse(false);

            if (!hasApprovedEstimate) {
                throw new IllegalArgumentException(
                        "Cannot assign job without approved estimate. Job status: " + jobStatus);
            }
        }
    }

    /**
     * Check for scheduling conflicts.
     * Throws AssignmentConflictException if conflicts are found.
     * 
     * @param excludeAssignmentId Assignment ID to exclude from conflict check (for reassignments)
     */
    private void checkSchedulingConflicts(
            Long jobId,
            Long technicianId,
            Long bayId,
            LocalDateTime start,
            LocalDateTime end,
            Long excludeAssignmentId) {

        // If this is a reassignment, we need to exclude the current assignment from checks
        // The repository queries only check ACTIVE assignments, so we're safe
        // However, if we're reassigning, the old assignment is still ACTIVE until we cancel it
        // So we check conflicts and then decide if they're acceptable

        if (excludeAssignmentId == null) {
            // New assignment - check all conflicts
            if (assignmentRepository.hasOverlappingTechnicianAssignment(technicianId, start, end)) {
                throw new AssignmentConflictException(
                        "Technician has overlapping assignment during the specified time period");
            }

            if (assignmentRepository.hasOverlappingBayAssignment(bayId, start, end)) {
                throw new AssignmentConflictException(
                        "Service bay has overlapping assignment during the specified time period");
            }

            if (assignmentRepository.hasOverlappingJobAssignment(jobId, start, end)) {
                throw new AssignmentConflictException(
                        "Job has overlapping assignment during the specified time period");
            }
        } else {
            // Reassignment - check conflicts but account for the current assignment
            // Get active assignments and filter out the one being reassigned
            List<JobAssignment> technicianAssignments = assignmentRepository
                    .findByTechnicianIdAndAssignmentStatus(technicianId, AssignmentStatus.ACTIVE)
                    .stream()
                    .filter(a -> !a.getId().equals(excludeAssignmentId))
                    .filter(a -> overlaps(a.getScheduledStart(), a.getScheduledEnd(), start, end))
                    .collect(Collectors.toList());

            if (!technicianAssignments.isEmpty()) {
                throw new AssignmentConflictException(
                        "Technician has overlapping assignment during the specified time period");
            }

            List<JobAssignment> bayAssignments = assignmentRepository
                    .findByBayIdAndAssignmentStatus(bayId, AssignmentStatus.ACTIVE)
                    .stream()
                    .filter(a -> !a.getId().equals(excludeAssignmentId))
                    .filter(a -> overlaps(a.getScheduledStart(), a.getScheduledEnd(), start, end))
                    .collect(Collectors.toList());

            if (!bayAssignments.isEmpty()) {
                throw new AssignmentConflictException(
                        "Service bay has overlapping assignment during the specified time period");
            }

            List<JobAssignment> jobAssignments = assignmentRepository
                    .findByJobId(jobId)
                    .stream()
                    .filter(a -> a.getAssignmentStatus() == AssignmentStatus.ACTIVE)
                    .filter(a -> !a.getId().equals(excludeAssignmentId))
                    .filter(a -> overlaps(a.getScheduledStart(), a.getScheduledEnd(), start, end))
                    .collect(Collectors.toList());

            if (!jobAssignments.isEmpty()) {
                throw new AssignmentConflictException(
                        "Job has overlapping assignment during the specified time period");
            }
        }
    }

    /**
     * Check if two time periods overlap.
     */
    private boolean overlaps(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Map entity to response DTO.
     */
    private JobAssignmentResponseDto mapToResponseDto(JobAssignment assignment) {
        return JobAssignmentResponseDto.builder()
                .id(assignment.getId())
                .jobId(assignment.getJob().getId())
                .jobCardNumber(assignment.getJob().getJobCardNumber())
                .technicianId(assignment.getTechnician().getId())
                .technicianCode(assignment.getTechnician().getTechnicianCode())
                .technicianName(assignment.getTechnician().getFirstName() + " " + 
                               assignment.getTechnician().getLastName())
                .bayId(assignment.getBay().getId())
                .bayCode(assignment.getBay().getBayCode())
                .bayName(assignment.getBay().getBayName())
                .scheduledStart(assignment.getScheduledStart())
                .scheduledEnd(assignment.getScheduledEnd())
                .assignmentStatus(assignment.getAssignmentStatus())
                .notes(assignment.getNotes())
                .assignedAt(assignment.getAssignedAt())
                .updatedAt(assignment.getUpdatedAt())
                .completedAt(assignment.getCompletedAt())
                .cancelledAt(assignment.getCancelledAt())
                .build();
    }
}

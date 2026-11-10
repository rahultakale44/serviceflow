package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.JobAssignmentRequestDto;
import com.serviceflow.backend.dto.JobAssignmentResponseDto;
import com.serviceflow.backend.dto.ReassignmentRequestDto;
import com.serviceflow.backend.service.JobAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for job assignment operations.
 * Provides endpoints for assigning technicians and service bays to jobs.
 * 
 * NOTE: Assignment lifecycle is separate from JobStatus lifecycle.
 * This controller does not update job status automatically.
 */
@RestController
@RequestMapping("/api/v1/job-assignments")
@RequiredArgsConstructor
@Slf4j
public class JobAssignmentController {

    private final JobAssignmentService assignmentService;

    /**
     * Create a new job assignment.
     * Assigns a technician and service bay to a job with scheduling.
     *
     * @param requestDto assignment data
     * @return created assignment with 201 status
     */
    @PostMapping
    public ResponseEntity<JobAssignmentResponseDto> createAssignment(
            @Valid @RequestBody JobAssignmentRequestDto requestDto) {
        log.info("POST /api/v1/job-assignments - Creating new assignment");
        JobAssignmentResponseDto response = assignmentService.createAssignment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get an assignment by ID.
     *
     * @param id assignment ID
     * @return assignment data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobAssignmentResponseDto> getAssignmentById(@PathVariable Long id) {
        log.info("GET /api/v1/job-assignments/{} - Fetching assignment", id);
        JobAssignmentResponseDto response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all assignments with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "scheduledStart")
     * @param direction sort direction (default "DESC")
     * @return paginated list of assignments with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<JobAssignmentResponseDto>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledStart") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("GET /api/v1/job-assignments - Fetching assignments");

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<JobAssignmentResponseDto> response = assignmentService.getAllAssignments(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments for a specific job.
     * Shows complete assignment history for the job.
     *
     * @param jobId job ID
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return paginated list of job assignments with 200 status
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<JobAssignmentResponseDto>> getAssignmentsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/job-assignments/job/{} - Fetching job assignments", jobId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assignedAt"));

        Page<JobAssignmentResponseDto> response = assignmentService.getAssignmentsByJob(jobId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments for a specific technician.
     * Shows complete assignment history for the technician.
     *
     * @param technicianId technician ID
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return paginated list of technician assignments with 200 status
     */
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Page<JobAssignmentResponseDto>> getAssignmentsByTechnician(
            @PathVariable Long technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/job-assignments/technician/{} - Fetching technician assignments", technicianId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledStart"));

        Page<JobAssignmentResponseDto> response = assignmentService.getAssignmentsByTechnician(technicianId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete an assignment.
     * Marks the assignment as completed with timestamp.
     *
     * @param id assignment ID
     * @return updated assignment with 200 status
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<JobAssignmentResponseDto> completeAssignment(@PathVariable Long id) {
        log.info("POST /api/v1/job-assignments/{}/complete - Completing assignment", id);
        JobAssignmentResponseDto response = assignmentService.completeAssignment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an assignment.
     * Marks the assignment as cancelled with timestamp.
     * Assignment history is preserved.
     *
     * @param id assignment ID
     * @return updated assignment with 200 status
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobAssignmentResponseDto> cancelAssignment(@PathVariable Long id) {
        log.info("POST /api/v1/job-assignments/{}/cancel - Cancelling assignment", id);
        JobAssignmentResponseDto response = assignmentService.cancelAssignment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Reassign a job to different technician or bay.
     * Cancels the old assignment and creates a new one.
     * Complete assignment history is preserved.
     *
     * @param id current assignment ID
     * @param requestDto new assignment data
     * @return new assignment with 200 status
     */
    @PostMapping("/{id}/reassign")
    public ResponseEntity<JobAssignmentResponseDto> reassignJob(
            @PathVariable Long id,
            @Valid @RequestBody ReassignmentRequestDto requestDto) {
        log.info("POST /api/v1/job-assignments/{}/reassign - Reassigning job", id);
        JobAssignmentResponseDto response = assignmentService.reassignJob(id, requestDto);
        return ResponseEntity.ok(response);
    }
}

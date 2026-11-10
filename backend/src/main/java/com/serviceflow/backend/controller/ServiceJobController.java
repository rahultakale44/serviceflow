package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.service.ServiceJobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class ServiceJobController {

    private final ServiceJobService serviceJobService;

    public ServiceJobController(ServiceJobService serviceJobService) {
        this.serviceJobService = serviceJobService;
    }

    @PostMapping
    public ResponseEntity<ServiceJobResponseDto> createJob(@Valid @RequestBody ServiceJobRequestDto requestDto) {
        ServiceJobResponseDto response = serviceJobService.createJobFromBooking(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceJobResponseDto> getJobById(@PathVariable Long id) {
        ServiceJobResponseDto response = serviceJobService.getJobById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job-card/{jobCardNumber}")
    public ResponseEntity<ServiceJobResponseDto> getJobByJobCardNumber(@PathVariable String jobCardNumber) {
        ServiceJobResponseDto response = serviceJobService.getJobByJobCardNumber(jobCardNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ServiceJobResponseDto> getJobByBookingId(@PathVariable Long bookingId) {
        ServiceJobResponseDto response = serviceJobService.getJobByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ServiceJobResponseDto>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ServiceJobResponseDto> response = serviceJobService.getAllJobs(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<Page<ServiceJobResponseDto>> getJobsByVehicleId(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ServiceJobResponseDto> response = serviceJobService.getJobsByVehicleId(vehicleId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceJobResponseDto> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateDto updateDto) {
        ServiceJobResponseDto response = serviceJobService.updateJobStatus(id, updateDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ServiceJobResponseDto> updateJobNotes(
            @PathVariable Long id,
            @Valid @RequestBody JobNotesUpdateDto notesDto) {
        ServiceJobResponseDto response = serviceJobService.updateJobNotes(id, notesDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/inspections")
    public ResponseEntity<JobInspectionResponseDto> addInspection(
            @PathVariable Long id,
            @Valid @RequestBody JobInspectionRequestDto requestDto) {
        JobInspectionResponseDto response = serviceJobService.addInspection(id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/inspections")
    public ResponseEntity<List<JobInspectionResponseDto>> getJobInspections(@PathVariable Long id) {
        List<JobInspectionResponseDto> response = serviceJobService.getJobInspections(id);
        return ResponseEntity.ok(response);
    }
}

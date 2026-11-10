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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceJobService {

    private final ServiceJobRepository serviceJobRepository;
    private final JobInspectionRepository jobInspectionRepository;
    private final ServiceBookingRepository serviceBookingRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    // Valid status transitions map
    private static final Map<JobStatus, List<JobStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(JobStatus.BOOKED, List.of(JobStatus.CHECKED_IN));
        VALID_TRANSITIONS.put(JobStatus.CHECKED_IN, List.of(JobStatus.INSPECTION));
        VALID_TRANSITIONS.put(JobStatus.INSPECTION, List.of(JobStatus.ESTIMATE_PENDING));
        VALID_TRANSITIONS.put(JobStatus.ESTIMATE_PENDING, List.of(JobStatus.AWAITING_APPROVAL));
        VALID_TRANSITIONS.put(JobStatus.AWAITING_APPROVAL, List.of(JobStatus.APPROVED, JobStatus.ESTIMATE_PENDING));
        VALID_TRANSITIONS.put(JobStatus.APPROVED, List.of(JobStatus.IN_SERVICE));
        VALID_TRANSITIONS.put(JobStatus.IN_SERVICE, List.of(JobStatus.QUALITY_CHECK));
        VALID_TRANSITIONS.put(JobStatus.QUALITY_CHECK, List.of(JobStatus.READY_FOR_DELIVERY, JobStatus.IN_SERVICE));
        VALID_TRANSITIONS.put(JobStatus.READY_FOR_DELIVERY, List.of(JobStatus.DELIVERED));
        VALID_TRANSITIONS.put(JobStatus.DELIVERED, List.of()); // Terminal state
    }

    public ServiceJobService(ServiceJobRepository serviceJobRepository,
                             JobInspectionRepository jobInspectionRepository,
                             ServiceBookingRepository serviceBookingRepository,
                             CustomerRepository customerRepository,
                             VehicleRepository vehicleRepository) {
        this.serviceJobRepository = serviceJobRepository;
        this.jobInspectionRepository = jobInspectionRepository;
        this.serviceBookingRepository = serviceBookingRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public ServiceJobResponseDto createJobFromBooking(ServiceJobRequestDto requestDto) {
        // Validate booking exists
        ServiceBooking booking = serviceBookingRepository.findById(requestDto.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with ID: " + requestDto.bookingId()));

        // Validate booking is in CHECKED_IN status
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException(
                    "Job card can only be created for bookings in CHECKED_IN status. Current status: " 
                            + booking.getBookingStatus());
        }

        // Check if job already exists for this booking
        if (serviceJobRepository.existsByBookingId(requestDto.bookingId())) {
            throw new DuplicateResourceException(
                    "Job card already exists for booking ID: " + requestDto.bookingId());
        }

        // Validate customer and vehicle exist
        Customer customer = customerRepository.findById(booking.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + booking.getCustomer().getId()));

        Vehicle vehicle = vehicleRepository.findById(booking.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with ID: " + booking.getVehicle().getId()));

        // Generate job card number
        String jobCardNumber = generateJobCardNumber();

        // Create service job
        ServiceJob serviceJob = new ServiceJob();
        serviceJob.setJobCardNumber(jobCardNumber);
        serviceJob.setBooking(booking);
        serviceJob.setCustomer(customer);
        serviceJob.setVehicle(vehicle);
        serviceJob.setJobStatus(JobStatus.CHECKED_IN); // Start with CHECKED_IN from booking
        serviceJob.setComplaintDescription(requestDto.complaintDescription());
        serviceJob.setInspectionNotes(requestDto.inspectionNotes());
        serviceJob.setCreatedAt(LocalDateTime.now());
        serviceJob.setUpdatedAt(LocalDateTime.now());

        ServiceJob savedJob = serviceJobRepository.save(serviceJob);

        return mapToResponseDto(savedJob);
    }

    @Transactional(readOnly = true)
    public ServiceJobResponseDto getJobById(Long id) {
        ServiceJob serviceJob = serviceJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found with ID: " + id));
        return mapToResponseDto(serviceJob);
    }

    @Transactional(readOnly = true)
    public ServiceJobResponseDto getJobByJobCardNumber(String jobCardNumber) {
        ServiceJob serviceJob = serviceJobRepository.findByJobCardNumber(jobCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service job not found with job card number: " + jobCardNumber));
        return mapToResponseDto(serviceJob);
    }

    @Transactional(readOnly = true)
    public ServiceJobResponseDto getJobByBookingId(Long bookingId) {
        ServiceJob serviceJob = serviceJobRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service job not found for booking ID: " + bookingId));
        return mapToResponseDto(serviceJob);
    }

    @Transactional(readOnly = true)
    public Page<ServiceJobResponseDto> getAllJobs(Pageable pageable) {
        return serviceJobRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ServiceJobResponseDto> getJobsByVehicleId(Long vehicleId, Pageable pageable) {
        // Validate vehicle exists
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        return serviceJobRepository.findByVehicleId(vehicleId, pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional
    public ServiceJobResponseDto updateJobStatus(Long id, JobStatusUpdateDto updateDto) {
        ServiceJob serviceJob = serviceJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found with ID: " + id));

        JobStatus currentStatus = serviceJob.getJobStatus();
        JobStatus newStatus = updateDto.jobStatus();

        // Validate status transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }

        // Update status
        serviceJob.setJobStatus(newStatus);
        serviceJob.setUpdatedAt(LocalDateTime.now());

        // Update timestamp fields based on status
        switch (newStatus) {
            case IN_SERVICE:
                if (serviceJob.getStartedAt() == null) {
                    serviceJob.setStartedAt(LocalDateTime.now());
                }
                break;
            case QUALITY_CHECK:
                serviceJob.setCompletedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                serviceJob.setDeliveredAt(LocalDateTime.now());
                break;
        }

        // Update technician notes if provided
        if (updateDto.notes() != null && !updateDto.notes().isBlank()) {
            String existingNotes = serviceJob.getTechnicianNotes();
            String updatedNotes = existingNotes != null 
                    ? existingNotes + "\n[" + LocalDateTime.now() + "] " + updateDto.notes()
                    : "[" + LocalDateTime.now() + "] " + updateDto.notes();
            serviceJob.setTechnicianNotes(updatedNotes);
        }

        ServiceJob updatedJob = serviceJobRepository.save(serviceJob);
        return mapToResponseDto(updatedJob);
    }

    @Transactional
    public ServiceJobResponseDto updateJobNotes(Long id, JobNotesUpdateDto notesDto) {
        ServiceJob serviceJob = serviceJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found with ID: " + id));

        if (notesDto.inspectionNotes() != null) {
            serviceJob.setInspectionNotes(notesDto.inspectionNotes());
        }

        if (notesDto.technicianNotes() != null) {
            serviceJob.setTechnicianNotes(notesDto.technicianNotes());
        }

        serviceJob.setUpdatedAt(LocalDateTime.now());

        ServiceJob updatedJob = serviceJobRepository.save(serviceJob);
        return mapToResponseDto(updatedJob);
    }

    @Transactional
    public JobInspectionResponseDto addInspection(Long jobId, JobInspectionRequestDto requestDto) {
        ServiceJob serviceJob = serviceJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found with ID: " + jobId));

        JobInspection inspection = new JobInspection();
        inspection.setJob(serviceJob);
        inspection.setInspectionCategory(requestDto.category());
        inspection.setItemName(requestDto.itemName());
        inspection.setCondition(requestDto.condition());
        inspection.setNotes(requestDto.notes());
        inspection.setInspectedAt(LocalDateTime.now());

        JobInspection savedInspection = jobInspectionRepository.save(inspection);

        return mapToInspectionResponseDto(savedInspection);
    }

    @Transactional(readOnly = true)
    public List<JobInspectionResponseDto> getJobInspections(Long jobId) {
        if (!serviceJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Service job not found with ID: " + jobId);
        }

        return jobInspectionRepository.findByJobIdOrderByInspectedAtDesc(jobId)
                .stream()
                .map(this::mapToInspectionResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isValidTransition(JobStatus from, JobStatus to) {
        if (from == to) {
            return true; // Same status is always allowed
        }
        List<JobStatus> allowedTransitions = VALID_TRANSITIONS.get(from);
        return allowedTransitions != null && allowedTransitions.contains(to);
    }

    @Transactional
    private synchronized String generateJobCardNumber() {
        LocalDate today = LocalDate.now();
        String datePrefix = "JC-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        
        // Find the last job card number for today
        String lastJobCardNumber = serviceJobRepository.findAll().stream()
                .map(ServiceJob::getJobCardNumber)
                .filter(num -> num.startsWith(datePrefix))
                .max(String::compareTo)
                .orElse(null);

        int nextSequence = 1;
        if (lastJobCardNumber != null) {
            String sequencePart = lastJobCardNumber.substring(datePrefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        return datePrefix + String.format("%05d", nextSequence);
    }

    private ServiceJobResponseDto mapToResponseDto(ServiceJob serviceJob) {
        List<JobInspectionResponseDto> inspections = jobInspectionRepository
                .findByJobIdOrderByInspectedAtDesc(serviceJob.getId())
                .stream()
                .map(this::mapToInspectionResponseDto)
                .collect(Collectors.toList());

        return new ServiceJobResponseDto(
                serviceJob.getId(),
                serviceJob.getJobCardNumber(),
                serviceJob.getBooking().getId(),
                serviceJob.getCustomer().getId(),
                serviceJob.getCustomer().getFirstName() + " " + serviceJob.getCustomer().getLastName(),
                serviceJob.getCustomer().getPhone(),
                serviceJob.getVehicle().getId(),
                serviceJob.getVehicle().getRegistrationNumber(),
                serviceJob.getVehicle().getMake(),
                serviceJob.getVehicle().getModel(),
                serviceJob.getJobStatus(),
                serviceJob.getComplaintDescription(),
                serviceJob.getInspectionNotes(),
                serviceJob.getTechnicianNotes(),
                serviceJob.getCreatedAt(),
                serviceJob.getUpdatedAt(),
                serviceJob.getStartedAt(),
                serviceJob.getCompletedAt(),
                serviceJob.getDeliveredAt(),
                inspections
        );
    }

    private JobInspectionResponseDto mapToInspectionResponseDto(JobInspection inspection) {
        return new JobInspectionResponseDto(
                inspection.getId(),
                inspection.getJob().getId(),
                inspection.getInspectionCategory(),
                inspection.getItemName(),
                inspection.getCondition(),
                inspection.getNotes(),
                inspection.getInspectedAt()
        );
    }
}

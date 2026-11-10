package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.AssignmentStatus;
import com.serviceflow.backend.entity.JobAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobAssignment entity.
 * Provides database access methods and custom queries for conflict detection.
 */
@Repository
public interface JobAssignmentRepository extends JpaRepository<JobAssignment, Long> {

    /**
     * Find all assignments for a specific job.
     */
    List<JobAssignment> findByJobId(Long jobId);

    /**
     * Find all assignments for a specific job with pagination.
     */
    Page<JobAssignment> findByJobId(Long jobId, Pageable pageable);

    /**
     * Find all assignments for a specific technician.
     */
    Page<JobAssignment> findByTechnicianId(Long technicianId, Pageable pageable);

    /**
     * Find all assignments for a specific service bay.
     */
    Page<JobAssignment> findByBayId(Long bayId, Pageable pageable);

    /**
     * Find active assignment for a specific job.
     */
    Optional<JobAssignment> findByJobIdAndAssignmentStatus(Long jobId, AssignmentStatus status);

    /**
     * Check if technician has overlapping active assignments.
     * Used for conflict detection when scheduling.
     */
    @Query("SELECT COUNT(a) > 0 FROM JobAssignment a " +
           "WHERE a.technician.id = :technicianId " +
           "AND a.assignmentStatus = 'ACTIVE' " +
           "AND ((a.scheduledStart < :end AND a.scheduledEnd > :start))")
    boolean hasOverlappingTechnicianAssignment(
            @Param("technicianId") Long technicianId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Check if service bay has overlapping active assignments.
     * Used for conflict detection when scheduling.
     */
    @Query("SELECT COUNT(a) > 0 FROM JobAssignment a " +
           "WHERE a.bay.id = :bayId " +
           "AND a.assignmentStatus = 'ACTIVE' " +
           "AND ((a.scheduledStart < :end AND a.scheduledEnd > :start))")
    boolean hasOverlappingBayAssignment(
            @Param("bayId") Long bayId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Check if job has overlapping active assignments.
     * Used for conflict detection when scheduling.
     */
    @Query("SELECT COUNT(a) > 0 FROM JobAssignment a " +
           "WHERE a.job.id = :jobId " +
           "AND a.assignmentStatus = 'ACTIVE' " +
           "AND ((a.scheduledStart < :end AND a.scheduledEnd > :start))")
    boolean hasOverlappingJobAssignment(
            @Param("jobId") Long jobId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Find active assignments for a technician.
     */
    List<JobAssignment> findByTechnicianIdAndAssignmentStatus(Long technicianId, AssignmentStatus status);

    /**
     * Find active assignments for a service bay.
     */
    List<JobAssignment> findByBayIdAndAssignmentStatus(Long bayId, AssignmentStatus status);
}

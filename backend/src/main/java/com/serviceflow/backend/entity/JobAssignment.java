package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JobAssignment entity representing the assignment of a technician and service bay
 * to a specific service job.
 * 
 * Tracks scheduling, assignment status, and maintains a complete history of all assignments.
 * Supports reassignment while preserving historical data.
 */
@Entity
@Table(name = "job_assignments",
        indexes = {
                @Index(columnList = "jobId", name = "idx_assignment_job"),
                @Index(columnList = "technicianId", name = "idx_assignment_technician"),
                @Index(columnList = "bayId", name = "idx_assignment_bay"),
                @Index(columnList = "assignmentStatus", name = "idx_assignment_status"),
                @Index(columnList = "scheduledStart", name = "idx_assignment_scheduled_start"),
                @Index(columnList = "scheduledEnd", name = "idx_assignment_scheduled_end")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobId", nullable = false, foreignKey = @ForeignKey(name = "fk_assignment_job"))
    private ServiceJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicianId", nullable = false, foreignKey = @ForeignKey(name = "fk_assignment_technician"))
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bayId", nullable = false, foreignKey = @ForeignKey(name = "fk_assignment_bay"))
    private ServiceBay bay;

    @Column(nullable = false)
    private LocalDateTime scheduledStart;

    @Column(nullable = false)
    private LocalDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus assignmentStatus = AssignmentStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime cancelledAt;
}

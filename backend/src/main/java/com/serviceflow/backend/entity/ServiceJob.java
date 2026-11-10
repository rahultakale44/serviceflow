package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceJob entity representing a workshop job card.
 * Created from a checked-in booking and tracks the complete service lifecycle.
 */
@Entity
@Table(name = "service_jobs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "jobCardNumber", name = "uk_job_card_number"),
                @UniqueConstraint(columnNames = "bookingId", name = "uk_job_booking")
        },
        indexes = {
                @Index(columnList = "jobCardNumber", name = "idx_job_card_number"),
                @Index(columnList = "bookingId", name = "idx_job_booking"),
                @Index(columnList = "customerId", name = "idx_job_customer"),
                @Index(columnList = "vehicleId", name = "idx_job_vehicle"),
                @Index(columnList = "jobStatus", name = "idx_job_status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String jobCardNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingId", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_job_booking"))
    private ServiceBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false, foreignKey = @ForeignKey(name = "fk_job_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleId", nullable = false, foreignKey = @ForeignKey(name = "fk_job_vehicle"))
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private JobStatus jobStatus = JobStatus.BOOKED;

    @Column(columnDefinition = "TEXT")
    private String complaintDescription;

    @Column(columnDefinition = "TEXT")
    private String inspectionNotes;

    @Column(columnDefinition = "TEXT")
    private String technicianNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobInspection> inspections = new ArrayList<>();

    /**
     * Helper method to add an inspection record.
     * Maintains bidirectional relationship.
     */
    public void addInspection(JobInspection inspection) {
        inspections.add(inspection);
        inspection.setJob(this);
    }
}

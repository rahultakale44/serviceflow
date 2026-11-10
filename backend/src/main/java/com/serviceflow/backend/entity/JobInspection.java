package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JobInspection entity representing a vehicle inspection record.
 * Multiple inspection records can be associated with a single job.
 */
@Entity
@Table(name = "job_inspections",
        indexes = {
                @Index(columnList = "jobId", name = "idx_inspection_job"),
                @Index(columnList = "inspectionCategory", name = "idx_inspection_category")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobId", nullable = false, foreignKey = @ForeignKey(name = "fk_inspection_job"))
    private ServiceJob job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionCategory inspectionCategory;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionCondition condition;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime inspectedAt;
}

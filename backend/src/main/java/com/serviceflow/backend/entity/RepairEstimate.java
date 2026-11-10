package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RepairEstimate entity representing a cost estimate for vehicle repair.
 * Multiple estimate versions can exist for a job (revisions), but only one
 * should be active for customer approval at a time.
 * 
 * Historical estimates are preserved and never modified after submission.
 */
@Entity
@Table(name = "repair_estimates",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "estimateNumber", name = "uk_estimate_number")
        },
        indexes = {
                @Index(columnList = "estimateNumber", name = "idx_estimate_number"),
                @Index(columnList = "jobId", name = "idx_estimate_job"),
                @Index(columnList = "estimateStatus", name = "idx_estimate_status"),
                @Index(columnList = "version", name = "idx_estimate_version")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String estimateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobId", nullable = false, foreignKey = @ForeignKey(name = "fk_estimate_job"))
    private ServiceJob job;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstimateStatus estimateStatus = EstimateStatus.DRAFT;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime rejectedAt;

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RepairEstimateItem> items = new ArrayList<>();

    /**
     * Helper method to add an estimate item.
     * Maintains bidirectional relationship.
     */
    public void addItem(RepairEstimateItem item) {
        items.add(item);
        item.setEstimate(this);
    }

    /**
     * Helper method to clear all items.
     */
    public void clearItems() {
        items.forEach(item -> item.setEstimate(null));
        items.clear();
    }
}

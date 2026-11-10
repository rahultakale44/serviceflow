package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * EstimateApproval entity representing customer approval or rejection decision.
 * Provides audit trail for estimate decisions.
 */
@Entity
@Table(name = "estimate_approvals",
        indexes = {
                @Index(columnList = "estimateId", name = "idx_approval_estimate"),
                @Index(columnList = "customerId", name = "idx_approval_customer"),
                @Index(columnList = "decision", name = "idx_approval_decision")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimateId", nullable = false, foreignKey = @ForeignKey(name = "fk_approval_estimate"))
    private RepairEstimate estimate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false, foreignKey = @ForeignKey(name = "fk_approval_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApprovalDecision decision;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime decisionTimestamp;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String customerComments;
}

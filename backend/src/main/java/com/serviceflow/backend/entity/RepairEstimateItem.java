package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * RepairEstimateItem entity representing a line item in a repair estimate.
 * Historical snapshots that preserve pricing even if catalogue changes.
 */
@Entity
@Table(name = "repair_estimate_items",
        indexes = {
                @Index(columnList = "estimateId", name = "idx_estimate_item_estimate")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairEstimateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimateId", nullable = false, foreignKey = @ForeignKey(name = "fk_estimate_item_estimate"))
    private RepairEstimate estimate;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstimateItemType itemType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    /**
     * Calculate line total based on unit price and quantity.
     */
    public void calculateLineTotal() {
        if (unitPrice != null && quantity != null) {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

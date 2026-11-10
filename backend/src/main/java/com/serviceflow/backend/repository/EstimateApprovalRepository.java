package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.EstimateApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstimateApprovalRepository extends JpaRepository<EstimateApproval, Long> {
    
    Optional<EstimateApproval> findByEstimateId(Long estimateId);
    
    boolean existsByEstimateId(Long estimateId);
    
    List<EstimateApproval> findByCustomerId(Long customerId);
}

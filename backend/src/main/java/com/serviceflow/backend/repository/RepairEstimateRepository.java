package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.EstimateStatus;
import com.serviceflow.backend.entity.RepairEstimate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairEstimateRepository extends JpaRepository<RepairEstimate, Long> {
    
    Optional<RepairEstimate> findByEstimateNumber(String estimateNumber);
    
    boolean existsByEstimateNumber(String estimateNumber);
    
    List<RepairEstimate> findByJobIdOrderByVersionDesc(Long jobId);
    
    Page<RepairEstimate> findByJobId(Long jobId, Pageable pageable);
    
    @Query("SELECT e FROM RepairEstimate e WHERE e.job.id = :jobId AND e.estimateStatus = :status ORDER BY e.version DESC")
    List<RepairEstimate> findByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") EstimateStatus status);
    
    @Query("SELECT e FROM RepairEstimate e WHERE e.job.id = :jobId AND e.version = (SELECT MAX(e2.version) FROM RepairEstimate e2 WHERE e2.job.id = :jobId)")
    Optional<RepairEstimate> findLatestVersionByJobId(@Param("jobId") Long jobId);
    
    @Query("SELECT e FROM RepairEstimate e WHERE e.job.id = :jobId AND e.estimateStatus = :status AND e.version = (SELECT MAX(e2.version) FROM RepairEstimate e2 WHERE e2.job.id = :jobId AND e2.estimateStatus = :status)")
    Optional<RepairEstimate> findLatestByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") EstimateStatus status);
}

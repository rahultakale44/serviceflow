package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.JobInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobInspectionRepository extends JpaRepository<JobInspection, Long> {
    List<JobInspection> findByJobIdOrderByInspectedAtDesc(Long jobId);
}

package com.jobpulse.job.repository;

import com.jobpulse.job.entity.Job;
import com.jobpulse.job.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByCompanyId(Long companyId);

    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.requiredSkills LEFT JOIN FETCH j.company WHERE j.id = :id")
    Job findByIdWithDetails(@Param("id") Long id);
}

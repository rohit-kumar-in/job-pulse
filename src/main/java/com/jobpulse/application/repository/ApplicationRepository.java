package com.jobpulse.application.repository;

import com.jobpulse.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query("SELECT a FROM Application a JOIN FETCH a.candidate c JOIN FETCH c.user JOIN FETCH a.job WHERE a.job.id = :jobId ORDER BY a.atsScore DESC")
    List<Application> findByJobIdOrderByAtsScoreDesc(@Param("jobId") Long jobId);

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByCandidateUserEmail(String email);
}

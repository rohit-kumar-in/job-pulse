package com.jobpulse.application.service;

import com.jobpulse.application.dto.ApplicationResponse;
import com.jobpulse.application.entity.Application;
import com.jobpulse.application.entity.ApplicationStatus;
import com.jobpulse.application.mapper.ApplicationMapper;
import com.jobpulse.application.repository.ApplicationRepository;
import com.jobpulse.candidate.entity.CandidateProfile;
import com.jobpulse.candidate.repository.CandidateProfileRepository;
import com.jobpulse.exception.DuplicateApplicationException;
import com.jobpulse.exception.ResourceNotFoundException;
import com.jobpulse.exception.ValidationException;
import com.jobpulse.job.entity.Job;
import com.jobpulse.job.entity.JobStatus;
import com.jobpulse.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final AtsScoringService atsScoringService;
    private final ApplicationMapper applicationMapper;

    public ApplicationService(ApplicationRepository applicationRepository,
                               JobRepository jobRepository,
                               CandidateProfileRepository candidateProfileRepository,
                               AtsScoringService atsScoringService,
                               ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.atsScoringService = atsScoringService;
        this.applicationMapper = applicationMapper;
    }

    @Transactional
    public ApplicationResponse applyToJob(Long jobId, String candidateEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Rule: Inactive jobs cannot receive applications
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new ValidationException("Inactive jobs cannot receive applications");
        }

        CandidateProfile candidate = candidateProfileRepository.findByUserEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for email: " + candidateEmail));

        // Rule: A candidate cannot apply twice for the same job
        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new DuplicateApplicationException("Candidate cannot apply twice for the same job");
        }

        // ATS Calculation
        double atsScore = atsScoringService.calculateScore(job.getRequiredSkills(), candidate.getSkills());

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .atsScore(atsScore)
                .status(ApplicationStatus.APPLIED)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application submitted - Application ID: {}, Job ID: {}, Candidate Email: {}, ATS Score: {}%",
                savedApplication.getId(), job.getId(), candidateEmail, atsScore);

        return applicationMapper.toDTO(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForJobSortedByAtsScore(Long jobId, String recruiterEmail) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }

        // Recruiters receive applications sorted by ATS score DESC
        List<Application> applications = applicationRepository.findByJobIdOrderByAtsScoreDesc(jobId);
        return applications.stream()
                .map(applicationMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String candidateEmail) {
        List<Application> applications = applicationRepository.findByCandidateUserEmail(candidateEmail);
        return applications.stream()
                .map(applicationMapper::toDTO)
                .toList();
    }
}

package com.jobpulse.application.mapper;

import com.jobpulse.application.dto.ApplicationResponse;
import com.jobpulse.application.entity.Application;
import com.jobpulse.candidate.mapper.CandidateProfileMapper;
import com.jobpulse.job.mapper.JobMapper;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    private final JobMapper jobMapper;
    private final CandidateProfileMapper candidateProfileMapper;

    public ApplicationMapper(JobMapper jobMapper, CandidateProfileMapper candidateProfileMapper) {
        this.jobMapper = jobMapper;
        this.candidateProfileMapper = candidateProfileMapper;
    }

    public ApplicationResponse toDTO(Application application) {
        if (application == null) return null;
        return ApplicationResponse.builder()
                .id(application.getId())
                .job(jobMapper.toDTO(application.getJob()))
                .candidate(candidateProfileMapper.toDTO(application.getCandidate()))
                .atsScore(application.getAtsScore())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}

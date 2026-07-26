package com.jobpulse.application.dto;

import com.jobpulse.application.entity.ApplicationStatus;
import com.jobpulse.candidate.dto.CandidateProfileDTO;
import com.jobpulse.job.dto.JobResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private JobResponse job;
    private CandidateProfileDTO candidate;
    private Double atsScore;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}

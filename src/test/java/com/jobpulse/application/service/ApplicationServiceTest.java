package com.jobpulse.application.service;

import com.jobpulse.application.dto.ApplicationResponse;
import com.jobpulse.application.entity.Application;
import com.jobpulse.application.entity.ApplicationStatus;
import com.jobpulse.application.mapper.ApplicationMapper;
import com.jobpulse.application.repository.ApplicationRepository;
import com.jobpulse.candidate.entity.CandidateProfile;
import com.jobpulse.candidate.repository.CandidateProfileRepository;
import com.jobpulse.exception.DuplicateApplicationException;
import com.jobpulse.exception.ValidationException;
import com.jobpulse.job.entity.Job;
import com.jobpulse.job.entity.JobStatus;
import com.jobpulse.job.repository.JobRepository;
import com.jobpulse.skill.entity.Skill;
import com.jobpulse.user.entity.Role;
import com.jobpulse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CandidateProfileRepository candidateProfileRepository;

    @Mock
    private AtsScoringService atsScoringService;

    @Mock
    private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private Job activeJob;
    private Job inactiveJob;
    private CandidateProfile candidate;
    private User candidateUser;

    @BeforeEach
    void setUp() {
        candidateUser = User.builder()
                .id(1L)
                .name("Jane Candidate")
                .email("jane@candidate.com")
                .role(Role.CANDIDATE)
                .build();

        candidate = CandidateProfile.builder()
                .id(10L)
                .user(candidateUser)
                .skills(Set.of(Skill.builder().name("Java").build()))
                .build();

        activeJob = Job.builder()
                .id(100L)
                .title("Java Developer")
                .status(JobStatus.ACTIVE)
                .requiredSkills(Set.of(Skill.builder().name("Java").build()))
                .build();

        inactiveJob = Job.builder()
                .id(101L)
                .title("Legacy Dev")
                .status(JobStatus.INACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should apply to active job successfully and calculate ATS score")
    void testApplyToJob_Success() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(activeJob));
        when(candidateProfileRepository.findByUserEmail("jane@candidate.com")).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByJobIdAndCandidateId(100L, 10L)).thenReturn(false);
        when(atsScoringService.calculateScore(any(), any())).thenReturn(100.0);

        Application savedApp = Application.builder()
                .id(500L)
                .job(activeJob)
                .candidate(candidate)
                .atsScore(100.0)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);
        when(applicationMapper.toDTO(savedApp)).thenReturn(ApplicationResponse.builder().id(500L).atsScore(100.0).build());

        ApplicationResponse response = applicationService.applyToJob(100L, "jane@candidate.com");

        assertNotNull(response);
        assertEquals(100.0, response.getAtsScore());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    @DisplayName("Should throw DuplicateApplicationException when candidate applies twice")
    void testApplyToJob_DuplicateApplication() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(activeJob));
        when(candidateProfileRepository.findByUserEmail("jane@candidate.com")).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByJobIdAndCandidateId(100L, 10L)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class, () ->
                applicationService.applyToJob(100L, "jane@candidate.com"));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when applying to an inactive job")
    void testApplyToJob_InactiveJob() {
        when(jobRepository.findById(101L)).thenReturn(Optional.of(inactiveJob));

        assertThrows(ValidationException.class, () ->
                applicationService.applyToJob(101L, "jane@candidate.com"));

        verify(applicationRepository, never()).save(any());
    }
}

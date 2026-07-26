package com.jobpulse.job.service;

import com.jobpulse.company.entity.Company;
import com.jobpulse.company.repository.CompanyRepository;
import com.jobpulse.exception.ResourceNotFoundException;
import com.jobpulse.exception.UnauthorizedException;
import com.jobpulse.job.dto.JobCreateRequest;
import com.jobpulse.job.dto.JobResponse;
import com.jobpulse.job.entity.Job;
import com.jobpulse.job.entity.JobStatus;
import com.jobpulse.job.mapper.JobMapper;
import com.jobpulse.job.repository.JobRepository;
import com.jobpulse.skill.entity.Skill;
import com.jobpulse.skill.service.SkillService;
import com.jobpulse.user.entity.Role;
import com.jobpulse.user.entity.User;
import com.jobpulse.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;
    private final JobMapper jobMapper;

    public JobService(JobRepository jobRepository,
                      CompanyRepository companyRepository,
                      UserRepository userRepository,
                      SkillService skillService,
                      JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.skillService = skillService;
        this.jobMapper = jobMapper;
    }

    @Transactional
    public JobResponse createJob(JobCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only recruiters can create jobs");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));

        Set<Skill> requiredSkills = skillService.getOrCreateSkills(request.getRequiredSkillNames());

        Job job = Job.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .experience(request.getExperience())
                .salary(request.getSalary())
                .status(JobStatus.ACTIVE)
                .requiredSkills(requiredSkills)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully: ID={}, Title={}, Company={}",
                savedJob.getId(), savedJob.getTitle(), company.getName());

        return jobMapper.toDTO(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobCreateRequest request, String userEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Set<Skill> requiredSkills = skillService.getOrCreateSkills(request.getRequiredSkillNames());

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setExperience(request.getExperience());
        job.setSalary(request.getSalary());
        job.setRequiredSkills(requiredSkills);

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated successfully: ID={}", updatedJob.getId());
        return jobMapper.toDTO(updatedJob);
    }

    @Transactional
    public void deleteJob(Long jobId, String userEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        job.setStatus(JobStatus.INACTIVE);
        jobRepository.save(job);
        log.info("Job status updated to INACTIVE for ID: {}", jobId);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));
        return jobMapper.toDTO(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getAllActiveJobs() {
        return jobRepository.findByStatus(JobStatus.ACTIVE).stream()
                .map(jobMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getMyPostedJobs(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return jobRepository.findAll().stream()
                .map(jobMapper::toDTO)
                .toList();
    }
}

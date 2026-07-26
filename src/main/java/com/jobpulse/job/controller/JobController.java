package com.jobpulse.job.controller;

import com.jobpulse.application.dto.ApplicationResponse;
import com.jobpulse.application.service.ApplicationService;
import com.jobpulse.job.dto.JobCreateRequest;
import com.jobpulse.job.dto.JobResponse;
import com.jobpulse.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Endpoints for managing and applying to job postings")
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    public JobController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Create a new job posting (Recruiter only)")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponse response = jobService.createJob(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Update an existing job posting (Recruiter only)")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponse response = jobService.updateJob(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Delete / Inactivate a job posting (Recruiter only)")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deleteJob(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Get jobs posted by recruiter")
    public ResponseEntity<List<JobResponse>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobResponse> jobs = jobService.getMyPostedJobs(userDetails.getUsername());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Get applicants for a job sorted by ATS score (highest first)")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ApplicationResponse> applications = applicationService.getApplicationsForJobSortedByAtsScore(id, userDetails.getUsername());
        return ResponseEntity.ok(applications);
    }

    @GetMapping
    @Operation(summary = "Browse all active job postings (Candidate / Public)")
    public ResponseEntity<List<JobResponse>> getAllActiveJobs() {
        List<JobResponse> jobs = jobService.getAllActiveJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed view of a job by ID (Candidate / Public)")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply to a job with one click (Candidate only)")
    public ResponseEntity<ApplicationResponse> applyToJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponse response = applicationService.applyToJob(id, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

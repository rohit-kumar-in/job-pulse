package com.jobpulse.application.controller;

import com.jobpulse.application.dto.ApplicationResponse;
import com.jobpulse.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Endpoints for managing job applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get all applications submitted by the logged-in candidate")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ApplicationResponse> response = applicationService.getMyApplications(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}

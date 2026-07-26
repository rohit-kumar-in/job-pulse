package com.jobpulse.skill.controller;

import com.jobpulse.skill.dto.SkillCreateRequest;
import com.jobpulse.skill.dto.SkillDTO;
import com.jobpulse.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Endpoints for managing global skills catalog")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Create a new skill in the catalog")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillCreateRequest request) {
        SkillDTO response = skillService.createSkill(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all available skills in the catalog")
    public ResponseEntity<List<SkillDTO>> getAllSkills() {
        List<SkillDTO> response = skillService.getAllSkills();
        return ResponseEntity.ok(response);
    }
}

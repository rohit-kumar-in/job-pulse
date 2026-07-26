package com.jobpulse.job.dto;

import com.jobpulse.company.dto.CompanyDTO;
import com.jobpulse.job.entity.JobStatus;
import com.jobpulse.skill.dto.SkillDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private Long id;
    private CompanyDTO company;
    private String title;
    private String description;
    private String location;
    private Integer experience;
    private BigDecimal salary;
    private JobStatus status;
    private Set<SkillDTO> requiredSkills;
    private LocalDateTime createdAt;
}

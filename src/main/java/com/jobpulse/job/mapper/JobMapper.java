package com.jobpulse.job.mapper;

import com.jobpulse.company.mapper.CompanyMapper;
import com.jobpulse.job.dto.JobResponse;
import com.jobpulse.job.entity.Job;
import com.jobpulse.skill.mapper.SkillMapper;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    private final CompanyMapper companyMapper;
    private final SkillMapper skillMapper;

    public JobMapper(CompanyMapper companyMapper, SkillMapper skillMapper) {
        this.companyMapper = companyMapper;
        this.skillMapper = skillMapper;
    }

    public JobResponse toDTO(Job job) {
        if (job == null) return null;
        return JobResponse.builder()
                .id(job.getId())
                .company(companyMapper.toDTO(job.getCompany()))
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .experience(job.getExperience())
                .salary(job.getSalary())
                .status(job.getStatus())
                .requiredSkills(skillMapper.toDTOSet(job.getRequiredSkills()))
                .createdAt(job.getCreatedAt())
                .build();
    }
}

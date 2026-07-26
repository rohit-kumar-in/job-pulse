package com.jobpulse.candidate.mapper;

import com.jobpulse.candidate.dto.CandidateProfileDTO;
import com.jobpulse.candidate.entity.CandidateProfile;
import com.jobpulse.skill.mapper.SkillMapper;
import org.springframework.stereotype.Component;

@Component
public class CandidateProfileMapper {

    private final SkillMapper skillMapper;

    public CandidateProfileMapper(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    public CandidateProfileDTO toDTO(CandidateProfile candidate) {
        if (candidate == null) return null;
        return CandidateProfileDTO.builder()
                .id(candidate.getId())
                .userId(candidate.getUser() != null ? candidate.getUser().getId() : null)
                .userName(candidate.getUser() != null ? candidate.getUser().getName() : null)
                .userEmail(candidate.getUser() != null ? candidate.getUser().getEmail() : null)
                .experience(candidate.getExperience())
                .location(candidate.getLocation())
                .resumeUrl(candidate.getResumeUrl())
                .skills(skillMapper.toDTOSet(candidate.getSkills()))
                .build();
    }
}

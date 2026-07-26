package com.jobpulse.candidate.dto;

import com.jobpulse.skill.dto.SkillDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer experience;
    private String location;
    private String resumeUrl;
    private Set<SkillDTO> skills;
}

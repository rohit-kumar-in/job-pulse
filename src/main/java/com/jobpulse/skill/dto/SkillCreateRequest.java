package com.jobpulse.skill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCreateRequest {

    @NotBlank(message = "Skill name is required")
    private String name;
}

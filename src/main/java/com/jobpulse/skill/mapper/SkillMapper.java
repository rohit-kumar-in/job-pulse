package com.jobpulse.skill.mapper;

import com.jobpulse.skill.dto.SkillDTO;
import com.jobpulse.skill.entity.Skill;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SkillMapper {

    public SkillDTO toDTO(Skill skill) {
        if (skill == null) return null;
        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .build();
    }

    public Set<SkillDTO> toDTOSet(Set<Skill> skills) {
        if (skills == null) return Set.of();
        return skills.stream().map(this::toDTO).collect(Collectors.toSet());
    }

    public Skill toEntity(SkillDTO dto) {
        if (dto == null) return null;
        return Skill.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}

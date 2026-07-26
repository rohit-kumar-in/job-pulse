package com.jobpulse.skill.service;

import com.jobpulse.exception.ResourceNotFoundException;
import com.jobpulse.exception.ValidationException;
import com.jobpulse.skill.dto.SkillCreateRequest;
import com.jobpulse.skill.dto.SkillDTO;
import com.jobpulse.skill.entity.Skill;
import com.jobpulse.skill.mapper.SkillMapper;
import com.jobpulse.skill.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillService(SkillRepository skillRepository, SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
    }

    @Transactional
    public SkillDTO createSkill(SkillCreateRequest request) {
        String skillName = request.getName().trim();
        if (skillRepository.findByNameIgnoreCase(skillName).isPresent()) {
            throw new ValidationException("Skill with name '" + skillName + "' already exists");
        }

        Skill skill = Skill.builder()
                .name(skillName)
                .build();

        Skill savedSkill = skillRepository.save(skill);
        log.info("Skill created: ID={}, Name={}", savedSkill.getId(), savedSkill.getName());
        return skillMapper.toDTO(savedSkill);
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toDTO)
                .toList();
    }

    @Transactional
    public Set<Skill> getOrCreateSkills(Set<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Skill> skills = new HashSet<>();
        for (String name : skillNames) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) continue;

            Skill skill = skillRepository.findByNameIgnoreCase(trimmedName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(trimmedName).build()));
            skills.add(skill);
        }

        return skills;
    }
}

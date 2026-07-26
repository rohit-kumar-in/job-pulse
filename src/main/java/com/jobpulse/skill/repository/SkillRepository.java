package com.jobpulse.skill.repository;

import com.jobpulse.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    Set<Skill> findByNameInIgnoreCase(Collection<String> names);
}

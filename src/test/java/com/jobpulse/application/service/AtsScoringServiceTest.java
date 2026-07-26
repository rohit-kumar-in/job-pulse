package com.jobpulse.application.service;

import com.jobpulse.skill.entity.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtsScoringServiceTest {

    private AtsScoringService atsScoringService;

    @BeforeEach
    void setUp() {
        atsScoringService = new AtsScoringService();
    }

    @Test
    @DisplayName("Should return 75.0% when candidate matches 3 out of 4 required skills")
    void testPartialMatch_75Percent() {
        Set<Skill> requiredSkills = Set.of(
                Skill.builder().name("Java").build(),
                Skill.builder().name("Spring Boot").build(),
                Skill.builder().name("PostgreSQL").build(),
                Skill.builder().name("Docker").build()
        );

        Set<Skill> candidateSkills = Set.of(
                Skill.builder().name("Java").build(),
                Skill.builder().name("Spring Boot").build(),
                Skill.builder().name("PostgreSQL").build()
        );

        double score = atsScoringService.calculateScore(requiredSkills, candidateSkills);

        assertEquals(75.0, score, 0.001);
    }

    @Test
    @DisplayName("Should return 100.0% when candidate has all required skills case-insensitively")
    void testFullMatch_CaseInsensitive() {
        Set<Skill> requiredSkills = Set.of(
                Skill.builder().name("Java").build(),
                Skill.builder().name("Spring Boot").build()
        );

        Set<Skill> candidateSkills = Set.of(
                Skill.builder().name("java").build(),
                Skill.builder().name("SPRING BOOT").build(),
                Skill.builder().name("AWS").build()
        );

        double score = atsScoringService.calculateScore(requiredSkills, candidateSkills);

        assertEquals(100.0, score, 0.001);
    }

    @Test
    @DisplayName("Should return 0.0% when candidate has no matching skills")
    void testZeroMatch() {
        Set<Skill> requiredSkills = Set.of(
                Skill.builder().name("Java").build(),
                Skill.builder().name("Docker").build()
        );

        Set<Skill> candidateSkills = Set.of(
                Skill.builder().name("Python").build(),
                Skill.builder().name("React").build()
        );

        double score = atsScoringService.calculateScore(requiredSkills, candidateSkills);

        assertEquals(0.0, score, 0.001);
    }

    @Test
    @DisplayName("Should return 100.0% when job requires no skills")
    void testNoRequiredSkills() {
        Set<Skill> requiredSkills = Set.of();
        Set<Skill> candidateSkills = Set.of(Skill.builder().name("Java").build());

        double score = atsScoringService.calculateScore(requiredSkills, candidateSkills);

        assertEquals(100.0, score, 0.001);
    }
}

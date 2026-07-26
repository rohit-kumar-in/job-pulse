package com.jobpulse.application.service;

import com.jobpulse.skill.entity.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AtsScoringService {

    private static final Logger log = LoggerFactory.getLogger(AtsScoringService.class);

    /**
     * Calculates ATS score as a percentage: (Matched Skills / Required Skills) * 100
     * Performs case-insensitive matching between candidate skills and job required skills.
     */
    public double calculateScore(Set<Skill> requiredSkills, Set<Skill> candidateSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            log.info("Job requires no specific skills. Assigning default ATS score: 100.0%");
            return 100.0;
        }

        if (candidateSkills == null || candidateSkills.isEmpty()) {
            log.info("Candidate possesses no registered skills. ATS score: 0.0%");
            return 0.0;
        }

        Set<String> normalizedRequired = requiredSkills.stream()
                .map(s -> s.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> normalizedCandidate = candidateSkills.stream()
                .map(s -> s.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        long matchCount = normalizedCandidate.stream()
                .filter(normalizedRequired::contains)
                .count();

        double score = ((double) matchCount / normalizedRequired.size()) * 100.0;

        // Round to 2 decimal places
        double roundedScore = Math.round(score * 100.0) / 100.0;

        log.info("ATS Calculation - Matched: {} / Required: {} -> Score: {}%",
                matchCount, normalizedRequired.size(), roundedScore);

        return roundedScore;
    }
}

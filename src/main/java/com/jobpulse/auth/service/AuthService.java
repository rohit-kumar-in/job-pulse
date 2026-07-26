package com.jobpulse.auth.service;

import com.jobpulse.auth.dto.AuthRequest;
import com.jobpulse.auth.dto.AuthResponse;
import com.jobpulse.auth.dto.RegisterRequest;
import com.jobpulse.candidate.entity.CandidateProfile;
import com.jobpulse.candidate.repository.CandidateProfileRepository;
import com.jobpulse.company.entity.Company;
import com.jobpulse.company.repository.CompanyRepository;
import com.jobpulse.exception.ValidationException;
import com.jobpulse.security.JwtTokenProvider;
import com.jobpulse.skill.entity.Skill;
import com.jobpulse.skill.service.SkillService;
import com.jobpulse.user.entity.Role;
import com.jobpulse.user.entity.User;
import com.jobpulse.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final SkillService skillService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       CandidateProfileRepository candidateProfileRepository,
                       SkillService skillService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.skillService = skillService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully. ID: {}, Email: {}, Role: {}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        if (request.getRole() == Role.RECRUITER) {
            String companyName = request.getCompanyName() != null ? request.getCompanyName() : request.getName() + " Tech";
            Company company = companyRepository.findByName(companyName)
                    .orElseGet(() -> companyRepository.save(Company.builder()
                            .name(companyName)
                            .website(request.getCompanyWebsite())
                            .build()));
            log.info("Recruiter associated with company: ID={}, Name={}", company.getId(), company.getName());
        } else if (request.getRole() == Role.CANDIDATE) {
            Set<Skill> candidateSkills = skillService.getOrCreateSkills(request.getSkills());

            CandidateProfile profile = CandidateProfile.builder()
                    .user(savedUser)
                    .experience(request.getExperience() != null ? request.getExperience() : 0)
                    .location(request.getLocation())
                    .resumeUrl(request.getResumeUrl())
                    .skills(candidateSkills)
                    .build();

            candidateProfileRepository.save(profile);
            log.info("Candidate profile created for User ID: {}", savedUser.getId());
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));

        String token = jwtTokenProvider.generateToken(authentication);
        log.info("User successfully logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}

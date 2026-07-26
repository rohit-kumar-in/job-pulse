package com.jobpulse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpulse.auth.dto.AuthRequest;
import com.jobpulse.auth.dto.AuthResponse;
import com.jobpulse.auth.dto.RegisterRequest;
import com.jobpulse.job.dto.JobCreateRequest;
import com.jobpulse.job.dto.JobResponse;
import com.jobpulse.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobPulseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete end-to-end recruitment flow: Register, Post Job, Candidate Apply, Sort Applicants by ATS score")
    void testEndToEndRecruitmentFlow() throws Exception {

        // 1. Register Recruiter
        RegisterRequest recruiterRegister = RegisterRequest.builder()
                .name("John Recruiter")
                .email("john@recruiter.com")
                .password("password123")
                .role(Role.RECRUITER)
                .companyName("Acme Tech")
                .companyWebsite("https://acme.tech")
                .build();

        MvcResult recruiterRegResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruiterRegister)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse recruiterAuth = objectMapper.readValue(
                recruiterRegResult.getResponse().getContentAsString(), AuthResponse.class);
        String recruiterToken = recruiterAuth.getToken();

        // 2. Create Job as Recruiter
        JobCreateRequest jobCreateRequest = JobCreateRequest.builder()
                .companyId(1L)
                .title("Senior Java Engineer")
                .description("Build scalable microservices with Java and Spring")
                .location("Remote")
                .experience(5)
                .salary(new BigDecimal("120000.00"))
                .requiredSkillNames(Set.of("Java", "Spring Boot", "PostgreSQL"))
                .build();

        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Senior Java Engineer"))
                .andReturn();

        JobResponse createdJob = objectMapper.readValue(
                jobResult.getResponse().getContentAsString(), JobResponse.class);
        Long jobId = createdJob.getId();

        // 3. Register Candidate 1 (Full Match: 3/3 = 100%)
        RegisterRequest candidate1Register = RegisterRequest.builder()
                .name("Alice Expert")
                .email("alice@test.com")
                .password("password123")
                .role(Role.CANDIDATE)
                .experience(6)
                .skills(Set.of("Java", "Spring Boot", "PostgreSQL"))
                .build();

        MvcResult candidate1Result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidate1Register)))
                .andExpect(status().isCreated())
                .andReturn();

        String candidate1Token = objectMapper.readValue(
                candidate1Result.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // 4. Register Candidate 2 (Partial Match: 1/3 = 33.33%)
        RegisterRequest candidate2Register = RegisterRequest.builder()
                .name("Bob Junior")
                .email("bob@test.com")
                .password("password123")
                .role(Role.CANDIDATE)
                .experience(1)
                .skills(Set.of("Java"))
                .build();

        MvcResult candidate2Result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidate2Register)))
                .andExpect(status().isCreated())
                .andReturn();

        String candidate2Token = objectMapper.readValue(
                candidate2Result.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // 5. Candidate 1 applies to Job
        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atsScore").value(100.0));

        // 6. Candidate 2 applies to Job
        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atsScore").value(33.33));

        // 7. Candidate 1 tries to apply AGAIN (Should fail with 409 Conflict)
        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isConflict());

        // 8. Recruiter retrieves applications for the job (Should be sorted by ATS score DESC)
        mockMvc.perform(get("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].atsScore").value(100.0))
                .andExpect(jsonPath("$[0].candidate.userEmail").value("alice@test.com"))
                .andExpect(jsonPath("$[1].atsScore").value(33.33))
                .andExpect(jsonPath("$[1].candidate.userEmail").value("bob@test.com"));
    }
}

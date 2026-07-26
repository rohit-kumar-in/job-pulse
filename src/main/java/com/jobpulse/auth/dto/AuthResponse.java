package com.jobpulse.auth.dto;

import com.jobpulse.user.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private Role role;
}

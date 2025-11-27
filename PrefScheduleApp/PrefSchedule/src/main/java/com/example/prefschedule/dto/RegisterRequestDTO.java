package com.example.prefschedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    private String role; // "ROLE_STUDENT", "ROLE_ADMIN", "ROLE_INSTRUCTOR"

    private String email;
    private Integer year; // doar pentru student
}

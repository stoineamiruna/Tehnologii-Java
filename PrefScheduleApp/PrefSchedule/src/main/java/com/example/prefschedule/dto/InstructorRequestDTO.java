package com.example.prefschedule.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InstructorRequestDTO {
    @NotBlank(message = "Instructor name cannot be blank — please provide the student's full name.")
    private String name;

    @Email(message = "Please provide a valid email address (e.g., instructor@example.com).")
    @NotBlank
    private String email;
}

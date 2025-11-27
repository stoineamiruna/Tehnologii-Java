package com.example.prefschedule.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentRequestDTO {
    @NotBlank(message = "Student code cannot be blank — please provide a valid code.")
    private String code;

    @NotBlank(message = "Student name cannot be blank — please provide the student's full name.")
    private String name;

    @Email(message = "Please provide a valid email address (e.g., student@example.com).")
    private String email;

    @NotNull(message = "Year of study must not be null — please specify a valid number.")
    @Min(value = 1, message = "Year of study must be at least 1.")
    private Integer year;
}

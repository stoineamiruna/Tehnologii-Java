package com.example.prefschedule.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StudentPreferenceRequestDTO {
    @NotNull(message = "Student ID must not be null — please specify an existing student.")
    private Long studentId;

    @NotNull(message = "Course ID must not be null — please specify an existing course.")
    private Long courseId;

    @Positive(message = "Preference order must be a positive number (e.g., 1, 2, 3...).")
    private int preferenceOrder;
}

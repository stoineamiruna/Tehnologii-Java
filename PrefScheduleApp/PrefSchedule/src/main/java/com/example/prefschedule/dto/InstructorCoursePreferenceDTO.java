package com.example.prefschedule.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCoursePreferenceDTO {

    private Long id;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Compulsory course abbreviation is required")
    private String compulsoryCourseAbbr;

    @NotNull(message = "Weight percentage is required")
    @Min(value = 0, message = "Weight percentage must be between 0 and 100")
    @Max(value = 100, message = "Weight percentage must be between 0 and 100")
    private Double weightPercentage;
}

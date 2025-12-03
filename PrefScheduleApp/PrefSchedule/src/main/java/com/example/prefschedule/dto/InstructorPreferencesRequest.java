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
public class InstructorPreferencesRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotEmpty(message = "At least one preference is required")
    private List<CompulsoryCourseWeightDTO> preferences;
}

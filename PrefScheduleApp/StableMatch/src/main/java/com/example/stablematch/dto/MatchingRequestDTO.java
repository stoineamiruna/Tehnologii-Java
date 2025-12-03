package com.example.stablematch.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class MatchingRequestDTO {
    @NotEmpty
    private List<StudentPreferenceDTO> studentPreferences;

    @NotEmpty
    private List<CourseCapacityDTO> courses;

    @NotEmpty
    private List<InstructorPreferenceDTO> instructorPreferences;
}
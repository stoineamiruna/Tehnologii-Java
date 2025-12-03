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
public class InstructorPreferencesResponse {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Map<String, Double> gradeWeights;
}

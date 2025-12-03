package com.example.prefschedule.dto.matching;
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
public class StudentPreferenceDTO {
    private String studentCode;
    private List<String> preferredCourses;
    private Map<String, Double> courseGrades;
}

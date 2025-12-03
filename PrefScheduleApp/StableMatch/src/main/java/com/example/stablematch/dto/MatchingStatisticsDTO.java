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
public class MatchingStatisticsDTO {
    private Integer totalStudents;
    private Integer assignedStudents;
    private Integer unassignedStudents;
    private Double averagePreferenceRank;
    private Map<String, Integer> courseAssignmentCounts;
}

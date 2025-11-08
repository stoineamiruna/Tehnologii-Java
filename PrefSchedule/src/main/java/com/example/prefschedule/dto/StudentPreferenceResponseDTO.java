package com.example.prefschedule.dto;

import lombok.Data;

@Data
public class StudentPreferenceResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private int preferenceOrder;
}

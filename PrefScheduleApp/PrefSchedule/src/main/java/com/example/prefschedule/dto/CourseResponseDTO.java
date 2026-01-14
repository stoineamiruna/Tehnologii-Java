package com.example.prefschedule.dto;

import lombok.Data;

@Data
public class CourseResponseDTO {
    private Long id;
    private String type;
    private String code;
    private String abbr;
    private String name;
    private Integer groupCount;
    private String description;
    private Long instructorId;
    private Long packId;
}

package com.example.prefschedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequestDTO {

    @NotBlank(message = "Course type is required")
    private String type;

    @NotBlank(message = "Course code is required")
    private String code;

    @NotBlank(message = "Course abbreviation is required")
    @Size(max = 10, message = "Abbreviation cannot exceed 10 characters")
    private String abbr;

    @NotBlank(message = "Course name is required")
    private String name;

    @NotNull(message = "Group count is required")
    private Integer groupCount;

    private String description;

    private Long instructorId; // optional for linking instructor
    private Long packId;       // optional for linking pack
}

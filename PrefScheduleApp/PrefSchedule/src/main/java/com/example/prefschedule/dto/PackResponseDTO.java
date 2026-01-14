package com.example.prefschedule.dto;

import lombok.Data;

@Data
public class PackResponseDTO {

    private Long id;
    private int year;
    private int semester;
    private String name;
    private int coursesCount; // number of courses in the pack
}

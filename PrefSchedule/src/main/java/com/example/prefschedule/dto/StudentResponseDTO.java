package com.example.prefschedule.dto;

import lombok.Data;

@Data
public class StudentResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String email;
    private Integer year;
}

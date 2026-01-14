package com.example.prefschedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PackRequestDTO {

    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be at least 2020")
    @Max(value = 2030, message = "Year must be at most 2030")
    private Integer year;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @NotBlank(message = "Pack name is required")
    private String name;
}

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
public class CourseCapacityDTO {
    private String courseCode;
    private Integer capacity;
    private String instructorId;
}

package com.example.prefschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private String username;
    private String fullName;
    private Set<String> roles;
    private Long studentId;   // null daca nu e student
    private Long instructorId; // null daca nu e instructor
}

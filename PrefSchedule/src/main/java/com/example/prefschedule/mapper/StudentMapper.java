package com.example.prefschedule.mapper;

import com.example.prefschedule.dto.StudentRequestDTO;
import com.example.prefschedule.dto.StudentResponseDTO;
import com.example.prefschedule.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    // din RequestDTO -> Entity
    public Student toEntity(StudentRequestDTO dto) {
        if (dto == null) return null;
        Student student = new Student();
        student.setCode(dto.getCode());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setYear(dto.getYear());
        return student;
    }

    // din Entity -> ResponseDTO
    public StudentResponseDTO toResponseDTO(Student student) {
        if (student == null) return null;
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setCode(student.getCode());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setYear(student.getYear());
        return dto;
    }
}

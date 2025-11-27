package com.example.prefschedule.mapper;

import com.example.prefschedule.dto.StudentPreferenceRequestDTO;
import com.example.prefschedule.dto.StudentPreferenceResponseDTO;
import com.example.prefschedule.entity.Course;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.entity.StudentPreference;
import org.springframework.stereotype.Component;

@Component
public class StudentPreferenceMapper {

    public StudentPreference toEntity(StudentPreferenceRequestDTO dto, Student student, Course course) {
        if (dto == null) return null;

        StudentPreference pref = new StudentPreference();
        pref.setStudent(student);
        pref.setCourse(course);
        pref.setPreferenceOrder(dto.getPreferenceOrder());
        return pref;
    }

    public StudentPreferenceResponseDTO toResponseDTO(StudentPreference pref) {
        if (pref == null) return null;

        StudentPreferenceResponseDTO dto = new StudentPreferenceResponseDTO();
        dto.setId(pref.getId());
        dto.setStudentId(pref.getStudent().getId());
        dto.setStudentName(pref.getStudent().getName());
        dto.setCourseId(pref.getCourse().getId());
        dto.setCourseName(pref.getCourse().getName());
        dto.setPreferenceOrder(pref.getPreferenceOrder());
        return dto;
    }
}

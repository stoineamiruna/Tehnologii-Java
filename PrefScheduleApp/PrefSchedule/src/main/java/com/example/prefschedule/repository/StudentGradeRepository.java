package com.example.prefschedule.repository;

import com.example.prefschedule.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {
    List<StudentGrade> findByStudentCode(String studentCode);
    List<StudentGrade> findByCourseCode(String courseCode);
    List<StudentGrade> findByStudentCodeAndCourseCode(String studentCode, String courseCode);
}

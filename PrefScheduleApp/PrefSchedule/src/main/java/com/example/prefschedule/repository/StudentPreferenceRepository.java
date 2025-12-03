package com.example.prefschedule.repository;

import com.example.prefschedule.entity.StudentPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Long> {
    List<StudentPreference> findByStudent_Id(Long studentId);

    @Query("SELECT sp FROM StudentPreference sp WHERE sp.student.id = :studentId AND sp.course.pack.id = :packId")
    List<StudentPreference> findByStudentIdAndCoursePackId(
            @Param("studentId") Long studentId,
            @Param("packId") Long packId);

    @Query("SELECT sp FROM StudentPreference sp WHERE sp.course.id = :courseId")
    List<StudentPreference> findByCourseId(@Param("courseId") Long courseId);
}

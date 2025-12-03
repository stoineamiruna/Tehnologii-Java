package com.example.prefschedule.repository;

import com.example.prefschedule.entity.InstructorCoursePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorCoursePreferenceRepository extends JpaRepository<InstructorCoursePreference, Long> {

    List<InstructorCoursePreference> findByCourseId(Long courseId);

    @Query("SELECT icp FROM InstructorCoursePreference icp WHERE icp.course.code = :courseCode")
    List<InstructorCoursePreference> findByCourseCode(@Param("courseCode") String courseCode);

    @Query("SELECT icp FROM InstructorCoursePreference icp " +
            "WHERE icp.course.instructor.id = :instructorId")
    List<InstructorCoursePreference> findByInstructorId(@Param("instructorId") Long instructorId);

    @Modifying
    @Query("DELETE FROM InstructorCoursePreference icp WHERE icp.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndCompulsoryCourseAbbr(Long courseId, String compulsoryCourseAbbr);
}

package com.example.prefschedule.repository;

import com.example.prefschedule.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByType(String type);
    @Query("SELECT c FROM Course c WHERE c.name LIKE %:keyword%")
    List<Course> searchByName(String keyword);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.groupCount = c.groupCount + 1 WHERE c.id = :id")
    void incrementGroupCount(Long id);

    @Query("""
    SELECT CASE WHEN UPPER(c.type) = 'COMPULSORY' THEN TRUE ELSE FALSE END 
    FROM Course c
    WHERE c.code = :courseCode
    """)
    Boolean isCompulsory(@Param("courseCode") String courseCode);
    Optional<Course> findByCode(String code);
    List<Course> findByPackId(Long packId);

    @Query("SELECT c FROM Course c WHERE c.pack.id = :packId AND c.type = 'OPTIONAL'")
    List<Course> findOptionalCoursesByPackId(@Param("packId") Long packId);

}

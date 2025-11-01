package com.example.prefschedule.repository;

import com.example.prefschedule.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByType(String type);
    @Query("SELECT c FROM Course c WHERE c.name LIKE %:keyword%")
    List<Course> searchByName(String keyword);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.groupCount = c.groupCount + 1 WHERE c.id = :id")
    void incrementGroupCount(Long id);
}

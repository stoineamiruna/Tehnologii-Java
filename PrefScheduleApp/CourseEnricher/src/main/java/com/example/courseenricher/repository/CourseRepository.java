package com.example.courseenricher.repository;

import com.example.courseenricher.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.pack WHERE c.code = :code")
    Optional<Course> findByCodeWithPack(String code);
}
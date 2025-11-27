package com.example.prefschedule.repository;

import com.example.prefschedule.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByYear(Integer year);

    @Query("SELECT s FROM Student s WHERE s.name LIKE %:name%")
    List<Student> searchByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.email = :email WHERE s.id = :id")
    void updateEmail(Long id, String email);

    Optional<Student> findByCode(String code);
}

package com.example.prefschedule.repository;

import com.example.prefschedule.entity.AppUser;
import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByStudentProfile(Student student);
    boolean existsByInstructorProfile(Instructor instructor);
}

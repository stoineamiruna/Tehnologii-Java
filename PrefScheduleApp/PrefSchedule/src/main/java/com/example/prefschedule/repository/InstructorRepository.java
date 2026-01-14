package com.example.prefschedule.repository;

import com.example.prefschedule.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    List<Instructor> findByName(String name);
    @Query("SELECT i FROM Instructor i WHERE i.email LIKE %:email%")
    List<Instructor> searchByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Instructor i SET i.name = :name WHERE i.id = :id")
    void updateName(Long id, String name);

    boolean existsByEmail(String email);
}

package com.example.prefschedule.repository;

import com.example.prefschedule.entity.StudentPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Long> {
    List<StudentPreference> findByStudent_Id(Long studentId);
}

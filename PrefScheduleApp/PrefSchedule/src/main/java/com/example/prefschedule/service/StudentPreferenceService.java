package com.example.prefschedule.service;

import com.example.prefschedule.entity.Student;
import com.example.prefschedule.entity.StudentPreference;
import com.example.prefschedule.repository.StudentPreferenceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StudentPreferenceService {
    private final StudentPreferenceRepository repo;

    public StudentPreferenceService(StudentPreferenceRepository repo) {
        this.repo = repo;
    }
    public Optional<StudentPreference> getById(Long id) {
        return repo.findById(id);
    }
    public List<StudentPreference> getAll() {
        return repo.findAll();
    }
    public List<StudentPreference> getByStudent_Id(Long studentId) {
        return repo.findByStudent_Id(studentId);
    }

    public StudentPreference save(StudentPreference pref) {
        if (!Objects.equals(pref.getStudent().getYear(), pref.getCourse().getPack().getYear())) {
            throw new RuntimeException("Course does not belong to the student's year");
        }

        return repo.save(pref);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

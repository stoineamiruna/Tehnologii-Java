package com.example.prefschedule.service;

import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    public InstructorService(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    public void updateName(Long id, String name) {
        instructorRepository.updateName(id, name);
    }
    public List<Instructor> getAll() {
        return instructorRepository.findAll();
    }

    public Instructor save(Instructor instructor) {
        return instructorRepository.save(instructor);
    }

    public Optional<Instructor> getById(Long id) {
        return instructorRepository.findById(id);
    }

    public Instructor updateInstructor(Long id, Instructor instructor) {
        Instructor existing = instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        existing.setName(instructor.getName());
        existing.setEmail(instructor.getEmail());
        return instructorRepository.save(existing);
    }

    public void deleteById(Long id) {
        instructorRepository.deleteById(id);
    }

    public void updateEmail(Long id, String email) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        instructor.setEmail(email);
        instructorRepository.save(instructor);
    }
}

package com.example.prefschedule.service;

import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    public InstructorService(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    public List<Instructor> getAll() {
        return instructorRepository.findAll();
    }

    public Instructor save(Instructor instructor) {
        return instructorRepository.save(instructor);
    }

    public void updateName(Long id, String name) {
        instructorRepository.updateName(id, name);
    }
}

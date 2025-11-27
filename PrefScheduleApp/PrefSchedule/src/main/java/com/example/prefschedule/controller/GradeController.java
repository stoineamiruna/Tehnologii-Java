package com.example.prefschedule.controller;

import com.example.prefschedule.entity.StudentGrade;
import com.example.prefschedule.repository.StudentGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grades")
public class GradeController {

    private final StudentGradeRepository repo;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public List<StudentGrade> getAll() {
        return repo.findAll();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public String uploadCsv(@RequestParam("file") MultipartFile file) throws Exception {

        BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        );

        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");

            StudentGrade g = new StudentGrade();
            g.setStudentCode(parts[0]);
            g.setCourseCode(parts[1]);
            g.setGrade(Double.parseDouble(parts[2]));

            repo.save(g);
        }

        return "CSV uploaded!";
    }
}

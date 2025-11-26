package com.example.quickgrade.controller;

import com.example.quickgrade.dto.GradeEvent;
import com.example.quickgrade.producer.GradeProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grades")
public class GradeController {

    @Autowired
    private GradeProducer producer; // Spring injectează bean

    @PostMapping("/publish")
    public String publish(@RequestBody GradeEvent event) {
        producer.sendGrade(event);
        return "Grade sent!";
    }
}


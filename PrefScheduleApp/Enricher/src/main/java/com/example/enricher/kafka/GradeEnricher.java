package com.example.enricher.kafka;

import com.example.enricher.dto.EnrichedGradeEvent;
import com.example.enricher.dto.GradeEvent;
import com.example.enricher.entity.Student;
import com.example.enricher.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GradeEnricher {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private KafkaTemplate<String, EnrichedGradeEvent> kafkaTemplate;

    @KafkaListener(topics = "raw-grades-topic", groupId = "enricher-group")
    public void enrichGrade(GradeEvent event) {
        System.out.println("📥 Enricher received: " + event);

        Student student = studentRepo.findByCode(event.getStudentCode())
                .orElseThrow(() -> new RuntimeException("Student not found: " + event.getStudentCode()));

        EnrichedGradeEvent enriched = new EnrichedGradeEvent(
                event.getStudentCode(),
                student.getName(),
                student.getYear(),
                event.getCourseCode(),
                event.getGrade()
        );

        kafkaTemplate.send("enriched-grades-topic", enriched);
        System.out.println("📤 Enricher sent: " + enriched);
    }
}
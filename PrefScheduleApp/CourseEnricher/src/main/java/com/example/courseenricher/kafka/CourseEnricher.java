package com.example.courseenricher.kafka;

import com.example.courseenricher.dto.EnrichedGradeEvent;
import com.example.courseenricher.dto.FullGradeEvent;
import com.example.courseenricher.entity.Course;
import com.example.courseenricher.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CourseEnricher {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private KafkaTemplate<String, FullGradeEvent> kafkaTemplate;

    @KafkaListener(
            topics = "enriched-grades-topic",
            groupId = "course-enricher-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void enrichCourse(EnrichedGradeEvent event) {
        System.out.println("📥 CourseEnricher received: " + event);

        Course course = courseRepo.findByCodeWithPack(event.getCourseCode())
                .orElseThrow(() -> new RuntimeException("Course not found: " + event.getCourseCode()));

        String semester = course.getPack() != null ? course.getPack().getSemester() : "N/A";

        FullGradeEvent full = new FullGradeEvent(
                event.getStudentCode(),
                event.getStudentName(),
                event.getYear(),
                event.getCourseCode(),
                course.getName(),
                semester,
                event.getGrade()
        );

        kafkaTemplate.send("grades_topic", full);
        System.out.println("📤 CourseEnricher sent: " + full);
    }
}

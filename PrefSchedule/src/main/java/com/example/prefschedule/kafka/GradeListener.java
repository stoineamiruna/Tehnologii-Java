package com.example.prefschedule.kafka;

import com.example.prefschedule.dto.GradeEvent;
import com.example.prefschedule.entity.StudentGrade;
import com.example.prefschedule.repository.CourseRepository;
import com.example.prefschedule.repository.StudentGradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GradeListener {

    @Autowired
    StudentGradeRepository repo;

    @Autowired
    CourseRepository courseRepo;

    @KafkaListener(
            topics = "grades_topic",
            groupId = "prefschedule-group",
            containerFactory = "kafkaListenerFactory"
    )
    public void consume(GradeEvent event) {
        System.out.println("📩 Received: " + event);

        try {
            Boolean isCompulsory = courseRepo.isCompulsory(event.getCourseCode());

            if (isCompulsory == null) {
                System.out.println("❌ Course not found: " + event.getCourseCode());
                throw new IllegalArgumentException("Course not found: " + event.getCourseCode());
            }

            if (isCompulsory) {
                StudentGrade sg = new StudentGrade();
                sg.setStudentCode(event.getStudentCode());
                sg.setCourseCode(event.getCourseCode());
                sg.setGrade(event.getGrade());
                repo.save(sg);
                System.out.println("✅ Saved compulsory grade");
            } else {
                System.out.println("⏭️ Skipped optional course");
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing: " + e.getMessage());
            throw e; // Pentru retry + DLQ
        }
    }
}

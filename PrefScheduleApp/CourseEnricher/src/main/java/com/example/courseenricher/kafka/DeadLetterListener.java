package com.example.courseenricher.kafka;

import com.example.courseenricher.dto.EnrichedGradeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterListener {

    @KafkaListener(
            topics = "enriched-grades-topic.DLT",
            groupId = "course-enricher-dlq-group"
    )
    public void handleDLQ(EnrichedGradeEvent event) {
        System.err.println("⚠️ DLQ Message received in CourseEnricher: " + event);
    }
}

package com.example.prefschedule.kafka;

import com.example.prefschedule.dto.FullGradeEvent;  // ✅ FullGradeEvent, nu GradeEvent
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterListener {

    @KafkaListener(
            topics = "grades_topic.DLT",
            groupId = "prefschedule-dlq-group",
            containerFactory = "dlqKafkaListenerFactory"
    )
    public void handleDLQ(FullGradeEvent event) {
        System.err.println("⚠️ Message moved to DLQ: " + event);
    }
}
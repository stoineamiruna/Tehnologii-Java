package com.example.prefschedule.kafka;

import com.example.prefschedule.dto.GradeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterListener {

    @KafkaListener(
            topics = "grades_topic.DLT",
            groupId = "prefschedule-dlq-group",
            containerFactory = "dlqKafkaListenerFactory"
    )
    public void handleDLQ(GradeEvent event) {
        System.err.println("⚠️ Message moved to DLQ: " + event);
    }
    //comanda de vericat ca apar fizic in grade_topic.DLT:
    //kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic grades_topic.DLT --from-beginning

}

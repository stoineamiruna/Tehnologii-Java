package com.example.quickgrade.producer;

import com.example.quickgrade.dto.GradeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class GradeProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GradeProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendGrade(GradeEvent event) {
        kafkaTemplate.send("grades_topic", event);
        System.out.println("Produced event = " + event);
    }
}

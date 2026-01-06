package com.university.eventsourcing;

import com.university.eventsourcing.service.ProjectionPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventSourcingCoreApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EventSourcingCoreApplication.class, args);

        // Initialize projection publisher
        ProjectionPublisher publisher = context.getBean(ProjectionPublisher.class);
        publisher.init();
    }
}

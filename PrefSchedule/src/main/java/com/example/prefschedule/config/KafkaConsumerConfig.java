package com.example.prefschedule.config;

import com.example.prefschedule.dto.GradeEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // ==================== Main Consumer ====================
    @Bean
    public ConsumerFactory<String, GradeEvent> consumerFactory() {
        JsonDeserializer<GradeEvent> jsonDeserializer = new JsonDeserializer<>(GradeEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<GradeEvent> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "prefschedule-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    // ==================== DLQ Consumer ====================
    @Bean
    public ConsumerFactory<String, GradeEvent> dlqConsumerFactory() {
        JsonDeserializer<GradeEvent> jsonDeserializer = new JsonDeserializer<>(GradeEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "prefschedule-dlq-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    // ==================== KafkaTemplate (for retries & DLQ) ====================
    @Bean
    public ProducerFactory<String, GradeEvent> dlqProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, GradeEvent> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    // ==================== Listener Factory ====================
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GradeEvent> kafkaListenerFactory(
            KafkaTemplate<String, GradeEvent> dlqKafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, GradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dlqKafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        errorHandler.addNotRetryableExceptions(org.springframework.kafka.support.serializer.DeserializationException.class);

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GradeEvent> dlqKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, GradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dlqConsumerFactory());
        return factory;
    }

}

/*
    COMENZI KAFKA:

    -pornire:
    cd C:\kafka\bin\windows
    .\kafka-server-start.bat ..\..\config\server.properties

    -listarea topic-urilor existente:
    .\kafka-topics.bat --list --bootstrap-server localhost:9092

    -creeaza topic nou daca nu exista:
    .\kafka-topics.bat --create --topic grades_topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
    .\kafka-topics.bat --create --topic grades_topic.DLT --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

    -sterge un topic:
    .\kafka-topics.bat --delete --topic grades_topic --bootstrap-server localhost:9092
    .\kafka-topics.bat --delete --topic grades_topic.DLT --bootstrap-server localhost:9092

    -verificarea mesajelor din topic:
    .\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic grades_topic --from-beginning
    .\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic grades_topic.DLT --from-beginning
 */
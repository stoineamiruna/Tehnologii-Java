package com.example.userservice.service;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.entity.OutboxEvent;
import com.example.userservice.entity.User;
import com.example.userservice.event.UserEvent;
import com.example.userservice.repository.OutboxEventRepository;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public User createUser(CreateUserRequest request) throws JsonProcessingException {
        // Step 1: Create and save the user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());

        User savedUser = userRepository.save(user);

        // Step 2: Create the event
        UserEvent userEvent = new UserEvent(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getAddress(),
                "USER_CREATED"
        );

        // Step 3: Insert event into Outbox table
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(String.valueOf(savedUser.getId()));
        outboxEvent.setEventType("USER_CREATED");
        outboxEvent.setPayload(objectMapper.writeValueAsString(userEvent));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setPublished(false);

        outboxEventRepository.save(outboxEvent);

        return savedUser;
    }
}
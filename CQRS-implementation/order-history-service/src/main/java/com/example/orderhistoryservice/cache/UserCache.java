package com.example.orderhistoryservice.cache;

import com.example.orderhistoryservice.event.UserEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserCache {

    private final Map<Long, UserEvent> users = new ConcurrentHashMap<>();

    public void put(Long userId, UserEvent userEvent) {
        users.put(userId, userEvent);
    }

    public UserEvent get(Long userId) {
        return users.get(userId);
    }
}
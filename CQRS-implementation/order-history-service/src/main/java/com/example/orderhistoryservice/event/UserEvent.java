package com.example.orderhistoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private Long userId;
    private String name;
    private String email;
    private String address;
    private String eventType;
}
package com.saga.order.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceResponse {
    private boolean success;
    private String message;
    private Object data;
}
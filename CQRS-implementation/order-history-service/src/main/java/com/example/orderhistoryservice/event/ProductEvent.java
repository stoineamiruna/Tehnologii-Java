package com.example.orderhistoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String eventType;
}
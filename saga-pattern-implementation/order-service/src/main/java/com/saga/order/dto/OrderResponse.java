package com.saga.order.dto;

import com.saga.order.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private OrderStatus status;
    private String message;
}
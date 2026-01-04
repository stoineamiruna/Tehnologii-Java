package com.saga.shipping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDto {
    private Long id;
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
}
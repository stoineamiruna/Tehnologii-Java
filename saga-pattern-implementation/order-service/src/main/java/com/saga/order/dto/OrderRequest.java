package com.saga.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class OrderRequest {
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
}
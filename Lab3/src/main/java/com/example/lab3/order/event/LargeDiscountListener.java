package com.example.lab3.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LargeDiscountListener {

    private final Logger log = LoggerFactory.getLogger(LargeDiscountListener.class);

    @EventListener
    public void onLargeDiscount(LargeDiscountEvent event) {
        log.warn("LargeDiscountEvent: customer={}, orderId={}, discount={}",
                event.getCustomer() != null ? event.getCustomer().getName() : "UNKNOWN",
                event.getOrder() != null ? event.getOrder().getId() : "N/A",
                event.getDiscount());
    }
}

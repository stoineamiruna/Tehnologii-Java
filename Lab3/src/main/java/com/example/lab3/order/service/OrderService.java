package com.example.lab3.order.service;

import com.example.lab3.order.event.LargeDiscountEvent;
import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;
import com.example.lab3.order.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderService {

    // Constructor injection
    private final DiscountService discountService;
    private final CustomerRepository customerRepository;

    // Field injection (demo, nu se recomanda in productie)
    @Autowired
    private DemoService fieldInjectedDemo;

    // Setter injection (poate fi folosit optional, de ex. pentru evenimente)
    private ApplicationEventPublisher publisher;

    private final Logger log = LoggerFactory.getLogger(OrderService.class);

    // prag peste care publicam eveniment
    private final BigDecimal largeDiscountThreshold = BigDecimal.valueOf(100);

    // Constructor injection

    public OrderService(DiscountService discountService,
                        CustomerRepository customerRepository,
                        ApplicationEventPublisher publisher) {
        this.discountService = discountService;
        this.customerRepository = customerRepository;
        this.publisher = publisher;
        log.info("Constructor injection done: discountService + customerRepository");
    }

    // Setter injection
    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        log.info("Setter injection done: ApplicationEventPublisher");
    }
    // Metoda de aplicare discount
    public BigDecimal applyDiscount(Order order) {
        Optional<Customer> custOpt = customerRepository.findById(order.getCustomerId());
        Customer customer = custOpt.orElse(null); // aspectul va verifica eligibilitatea

        BigDecimal discount = discountService.calculateDiscount(customer, order);

        // log: method name, customer name, discount amount
        String customerName = (customer != null) ? customer.getName() : "UNKNOWN";
        log.info("applyDiscount called - customer='{}', discount={}", customerName, discount);

        if (discount.compareTo(largeDiscountThreshold) > 0) {
            // public eveniment
            publisher.publishEvent(new LargeDiscountEvent(this, order, customer, discount));
        }

        return discount;
    }
}

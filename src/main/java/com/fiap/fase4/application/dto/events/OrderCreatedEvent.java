package com.fiap.fase4.application.dto.events;

import com.fiap.fase4.application.dto.CustomerDTO;
import com.fiap.fase4.application.dto.ItemDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event produced by the Order Service when a new order is placed.
 * The Billing Service consumes this to initiate the payment process.
 */
public record OrderCreatedEvent(
    String orderId,
    String customerId,
    BigDecimal totalAmount,
    List<ItemDTO> items,
    CustomerDTO customer,
    LocalDateTime createdAt
) {}

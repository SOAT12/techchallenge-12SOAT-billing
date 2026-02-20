package com.fiap.fase4.application.dto.events;

import java.time.LocalDateTime;

/**
 * Event produced when an order is cancelled in the SAGA.
 * The Billing Service consumes this to trigger a compensating transaction (refund/cancel payment).
 */
public record OrderCancelledEvent(
    String orderId,
    String reason,
    LocalDateTime cancelledAt
) {}

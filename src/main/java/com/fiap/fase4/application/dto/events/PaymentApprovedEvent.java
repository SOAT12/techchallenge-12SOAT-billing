package com.fiap.fase4.application.dto.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event produced by the Billing Service when a payment is successfully processed.
 * Other services (like Order or Production) listen to this to advance the SAGA.
 */
public record PaymentApprovedEvent(
    String orderId,
    String paymentId,
    String externalReference,
    BigDecimal amount,
    String paymentMethod,
    LocalDateTime approvedAt
) {}

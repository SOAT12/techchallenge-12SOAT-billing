package com.fiap.fase4.application.dto.events;

import java.time.LocalDateTime;

/**
 * Event produced by the Billing Service when a payment fails or is rejected.
 * The Order Service listens to this to trigger a compensating transaction (order cancellation).
 */
public record PaymentFailedEvent(
    String orderId,
    String paymentId,
    String reason,
    String statusDetail,
    LocalDateTime failedAt
) {}

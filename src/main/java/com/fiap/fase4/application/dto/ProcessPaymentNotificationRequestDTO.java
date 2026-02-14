package com.fiap.fase4.application.dto;

public record ProcessPaymentNotificationRequestDTO (
    String resourceType, // e.g., "payment"
    String resourceId    // e.g., the payment ID
) {}

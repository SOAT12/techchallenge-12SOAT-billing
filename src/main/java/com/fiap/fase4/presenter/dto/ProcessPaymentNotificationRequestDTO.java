package com.fiap.fase4.presenter.dto;

public record ProcessPaymentNotificationRequestDTO (
    String resourceType, // e.g., "payment"
    String resourceId    // e.g., the payment ID
) {}


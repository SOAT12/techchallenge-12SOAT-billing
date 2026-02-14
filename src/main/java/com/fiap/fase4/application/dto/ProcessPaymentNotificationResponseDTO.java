package com.fiap.fase4.application.dto;

public record ProcessPaymentNotificationResponseDTO(
        boolean success,
        String updatedStatus // e.g., "APPROVED", "REJECTED"
) {}

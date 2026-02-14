package com.fiap.fase4.presenter.dto;

public record ProcessPaymentNotificationResponseDTO(
        boolean success,
        String updatedStatus // e.g., "APPROVED", "REJECTED"
) {}

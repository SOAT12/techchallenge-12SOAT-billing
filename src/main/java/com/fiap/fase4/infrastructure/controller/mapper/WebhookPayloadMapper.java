package com.fiap.fase4.infrastructure.controller.mapper;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.infrastructure.controller.dto.MercadoPagoNotificationDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Maps and validates incoming webhook payloads to application use case input.
 * Keeps HTTP/contract validation in the infrastructure layer.
 */
@Component
public class WebhookPayloadMapper {

    private static final String PAYMENT_TYPE = "payment";

    /**
     * Converts a Mercado Pago webhook payload to a use case request if valid.
     * Returns empty for invalid or non-payment notifications (caller may respond 200 to avoid retries).
     */
    public Optional<ProcessPaymentNotificationRequestDTO> toProcessPaymentRequest(MercadoPagoNotificationDTO payload) {
        if (payload == null || payload.getData() == null || payload.getData().getId() == null || payload.getType() == null) {
            return Optional.empty();
        }
        if (!PAYMENT_TYPE.equalsIgnoreCase(payload.getType())) {
            return Optional.empty();
        }
        return Optional.of(new ProcessPaymentNotificationRequestDTO(
                payload.getType(),
                payload.getData().getId()
        ));
    }
}

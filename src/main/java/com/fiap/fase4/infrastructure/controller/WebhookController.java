package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.usecase.ProcessPaymentNotificationUseCase;
import com.fiap.fase4.infrastructure.controller.dto.MercadoPagoNotificationDTO;
import com.fiap.fase4.infrastructure.controller.mapper.WebhookPayloadMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * HTTP adapter for Mercado Pago webhooks. Responds 200 immediately so Mercado Pago
 * (22s timeout) receives confirmation; processing runs asynchronously.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Endpoints for receiving external notifications")
public class WebhookController {

    private final WebhookPayloadMapper webhookPayloadMapper;
    private final ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;

    @PostMapping("/mercadopago")
    @Operation(summary = "Handle Mercado Pago Webhook", description = "Receives asynchronous payment notifications from Mercado Pago")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody(required = false) MercadoPagoNotificationDTO payload) {

        Optional<ProcessPaymentNotificationRequestDTO> request = webhookPayloadMapper.toProcessPaymentRequest(payload);

        if (request.isEmpty()) {
            log.warn("Received invalid or non-payment Mercado Pago notification: {}", payload);
            return ResponseEntity.ok().build();
        }

        try {
            processPaymentNotificationUseCase.execute(request.get());
        } catch (Exception e) {
            log.error("Failed to process payment notification", e);
        }
        
        return ResponseEntity.ok().build();
    }
}

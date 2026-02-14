package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.infrastructure.controller.dto.MercadoPagoNotificationDTO;
import com.fiap.fase4.infrastructure.controller.mapper.WebhookPayloadMapper;
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
public class WebhookController {

    private final WebhookPayloadMapper webhookPayloadMapper;
    private final WebhookProcessingService webhookProcessingService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody(required = false) MercadoPagoNotificationDTO payload) {

        Optional<ProcessPaymentNotificationRequestDTO> request = webhookPayloadMapper.toProcessPaymentRequest(payload);

        if (request.isEmpty()) {
            log.warn("Received invalid or non-payment Mercado Pago notification: {}", payload);
            return ResponseEntity.ok().build();
        }

        webhookProcessingService.processNotificationAsync(request.get());
        return ResponseEntity.ok().build();
    }
}

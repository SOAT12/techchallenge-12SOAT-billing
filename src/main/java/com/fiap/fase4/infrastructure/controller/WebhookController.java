package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.MercadoPagoNotificationDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.application.usecase.ProcessPaymentNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody(required = false) MercadoPagoNotificationDTO inputData) {

        if (inputData == null || inputData.getData() == null || inputData.getData().getId() == null || inputData.getType() == null) {
            log.warn("Received invalid Mercado Pago notification: {}", inputData);
            return ResponseEntity.ok().build(); 
        }

        String resourceId = inputData.getData().getId(); 
        String resourceType = inputData.getType(); 

        if (!"payment".equalsIgnoreCase(resourceType)) {
            log.info("Ignoring non-payment notification type: {}", resourceType);
            return ResponseEntity.ok().build(); 
        }

        try {
            var request = new ProcessPaymentNotificationRequestDTO(resourceType, resourceId);
            var result = processPaymentNotificationUseCase.execute(request);

            if (result.success()) {
                log.info("Payment {} processed successfully. Status: {}", resourceId, result.updatedStatus());
            } else {
                log.warn("Payment {} processing returned failure. Status: {}", resourceId, result.updatedStatus());
            }

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Error processing payment notification {}: {}", inputData, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

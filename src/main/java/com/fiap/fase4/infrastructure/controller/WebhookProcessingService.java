package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.application.usecase.ProcessPaymentNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Processes webhook notifications asynchronously so the HTTP endpoint can
 * respond 200 quickly (Mercado Pago requires a response within 22 seconds).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessingService {

    private final ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;

    @Async
    public void processNotificationAsync(ProcessPaymentNotificationRequestDTO request) {
        try {
            ProcessPaymentNotificationResponseDTO result = processPaymentNotificationUseCase.execute(request);
            if (result.success()) {
                log.info("Payment {} processed successfully. Status: {}", request.resourceId(), result.updatedStatus());
            } else {
                log.warn("Payment {} processing returned failure. Status: {}", request.resourceId(), result.updatedStatus());
            }
        } catch (Exception ex) {
            log.error("Error processing payment notification for resourceId: {}", request.resourceId(), ex);
        }
    }
}

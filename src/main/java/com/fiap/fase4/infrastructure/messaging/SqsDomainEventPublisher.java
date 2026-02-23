package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.events.PaymentApprovedEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;
import com.fiap.fase4.application.gateway.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsDomainEventPublisher implements DomainEventPublisher {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.os-status-update-queue-url:http://localhost:4566/000000000000/os-status-update-event}")
    private String osStatusUpdateQueueUrl;

    @Override
    public void publishPaymentApprovedEvent(PaymentApprovedEvent event) {
        log.info("Publishing PaymentApprovedEvent to SQS for order: {}", event.orderId());
        try {
            // Include a type header or wrapper so the OS API knows what event this is,
            // or just rely on the JSON payload structure.
            String message = objectMapper.writeValueAsString(event);
            
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(osStatusUpdateQueueUrl)
                    .messageBody(message)
                    // Optional: add message attributes if your consumer requires them
                    .build();

            sqsAsyncClient.sendMessage(request).whenComplete((response, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentApprovedEvent for order: {}", event.orderId(), ex);
                } else {
                    log.info("Successfully published PaymentApprovedEvent for order: {} with MessageId: {}", event.orderId(), response.messageId());
                }
            });
        } catch (Exception e) {
            log.error("Error serializing PaymentApprovedEvent for order: {}", event.orderId(), e);
        }
    }

    @Override
    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent to SQS for order: {}", event.orderId());
        try {
            String message = objectMapper.writeValueAsString(event);
            
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(osStatusUpdateQueueUrl)
                    .messageBody(message)
                    .build();

            sqsAsyncClient.sendMessage(request).whenComplete((response, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentFailedEvent for order: {}", event.orderId(), ex);
                } else {
                    log.info("Successfully published PaymentFailedEvent for order: {} with MessageId: {}", event.orderId(), response.messageId());
                }
            });
        } catch (Exception e) {
            log.error("Error serializing PaymentFailedEvent for order: {}", event.orderId(), e);
        }
    }
}

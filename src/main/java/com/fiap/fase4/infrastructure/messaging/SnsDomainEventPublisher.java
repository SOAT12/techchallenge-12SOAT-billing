package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.events.PaymentApprovedEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;
import com.fiap.fase4.application.gateway.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsDomainEventPublisher implements DomainEventPublisher {

    private final SnsAsyncClient snsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sns.payment-approved-topic-arn}")
    private String paymentApprovedTopicArn;

    @Value("${app.sns.payment-failed-topic-arn}")
    private String paymentFailedTopicArn;

    @Override
    public void publishPaymentApprovedEvent(PaymentApprovedEvent event) {
        log.info("Attempting to publish PaymentApprovedEvent to SNS for order: {}", event.orderId());
        try {
            String message = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(paymentApprovedTopicArn)
                    .message(message)
                    .build();

            // Use get() with timeout to fail fast and log if AWS is unreachable
            snsAsyncClient.publish(request).get(5, TimeUnit.SECONDS);
            log.info("Successfully published PaymentApprovedEvent for order: {}", event.orderId());
        } catch (TimeoutException e) {
            log.error("Timeout while publishing PaymentApprovedEvent to SNS for order: {}. Check VPC Endpoints or NAT Gateway.", event.orderId(), e);
        } catch (Exception e) {
            log.error("Failed to publish PaymentApprovedEvent for order: {}", event.orderId(), e);
        }
    }

    @Override
    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Attempting to publish PaymentFailedEvent to SNS for order: {}", event.orderId());
        try {
            String message = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(paymentFailedTopicArn)
                    .message(message)
                    .build();

            // Use get() with timeout to fail fast and log if AWS is unreachable
            snsAsyncClient.publish(request).get(5, TimeUnit.SECONDS);
            log.info("Successfully published PaymentFailedEvent for order: {}", event.orderId());
        } catch (TimeoutException e) {
            log.error("Timeout while publishing PaymentFailedEvent to SNS for order: {}. Check VPC Endpoints or NAT Gateway.", event.orderId(), e);
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailedEvent for order: {}", event.orderId(), e);
        }
    }
}

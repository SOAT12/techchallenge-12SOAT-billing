package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.events.PaymentApprovedEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsDomainEventPublisherTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsDomainEventPublisher sqsDomainEventPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sqsDomainEventPublisher, "osStatusUpdateQueueUrl", "http://test-queue");
    }

    @Test
    void publishPaymentApprovedEvent_shouldPublishSuccessfully() throws JsonProcessingException {
        PaymentApprovedEvent event = new PaymentApprovedEvent("ORDER-123", "PAY-123", "12345", BigDecimal.TEN, "PIX", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<SendMessageResponse> future = CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("MSG-123").build());
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        sqsDomainEventPublisher.publishPaymentApprovedEvent(event);

        verify(sqsAsyncClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishPaymentApprovedEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {
        PaymentApprovedEvent event = new PaymentApprovedEvent("ORDER-123", "PAY-123", "12345", BigDecimal.TEN, "PIX", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Error") {});

        sqsDomainEventPublisher.publishPaymentApprovedEvent(event);

        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishPaymentFailedEvent_shouldPublishSuccessfully() throws JsonProcessingException {
        PaymentFailedEvent event = new PaymentFailedEvent("ORDER-123", "PAY-123", "Error details", "detail", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<SendMessageResponse> future = CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("MSG-123").build());
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        sqsDomainEventPublisher.publishPaymentFailedEvent(event);

        verify(sqsAsyncClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishPaymentFailedEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {
        PaymentFailedEvent event = new PaymentFailedEvent("ORDER-123", "PAY-123", "Error details", "detail", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Error") {});

        sqsDomainEventPublisher.publishPaymentFailedEvent(event);

        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
    }
}

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
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnsDomainEventPublisherTest {

    @Mock
    private SnsAsyncClient snsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SnsDomainEventPublisher snsDomainEventPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(snsDomainEventPublisher, "paymentApprovedTopicArn", "arn:aws:sns:us-east-1:000000000000:payment-approved-topic");
        ReflectionTestUtils.setField(snsDomainEventPublisher, "paymentFailedTopicArn", "arn:aws:sns:us-east-1:000000000000:payment-failed-topic");
    }

    @Test
    void publishPaymentApprovedEvent_shouldPublishSuccessfully() throws JsonProcessingException {
        PaymentApprovedEvent event = new PaymentApprovedEvent("ORDER-123", "PAY-123", "12345", BigDecimal.TEN, "PIX", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<PublishResponse> future = CompletableFuture.completedFuture(PublishResponse.builder().messageId("MSG-123").build());
        when(snsAsyncClient.publish(any(PublishRequest.class))).thenReturn(future);

        snsDomainEventPublisher.publishPaymentApprovedEvent(event);

        verify(snsAsyncClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void publishPaymentApprovedEvent_shouldHandleTimeoutException() throws JsonProcessingException {
        PaymentApprovedEvent event = new PaymentApprovedEvent("ORDER-123", "PAY-123", "12345", BigDecimal.TEN, "PIX", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<PublishResponse> future = new CompletableFuture<PublishResponse>() {
            @Override
            public PublishResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new TimeoutException("Timeout");
            }
        };
        when(snsAsyncClient.publish(any(PublishRequest.class))).thenReturn(future);

        snsDomainEventPublisher.publishPaymentApprovedEvent(event);

        verify(snsAsyncClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void publishPaymentApprovedEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {
        PaymentApprovedEvent event = new PaymentApprovedEvent("ORDER-123", "PAY-123", "12345", BigDecimal.TEN, "PIX", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Error") {});

        snsDomainEventPublisher.publishPaymentApprovedEvent(event);

        verify(snsAsyncClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void publishPaymentFailedEvent_shouldPublishSuccessfully() throws JsonProcessingException {
        PaymentFailedEvent event = new PaymentFailedEvent("ORDER-123", "PAY-123", "Error details", "detail", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<PublishResponse> future = CompletableFuture.completedFuture(PublishResponse.builder().messageId("MSG-123").build());
        when(snsAsyncClient.publish(any(PublishRequest.class))).thenReturn(future);

        snsDomainEventPublisher.publishPaymentFailedEvent(event);

        verify(snsAsyncClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void publishPaymentFailedEvent_shouldHandleTimeoutException() throws JsonProcessingException {
        PaymentFailedEvent event = new PaymentFailedEvent("ORDER-123", "PAY-123", "Error details", "detail", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":\"ORDER-123\"}");

        CompletableFuture<PublishResponse> future = new CompletableFuture<PublishResponse>() {
            @Override
            public PublishResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new TimeoutException("Timeout");
            }
        };
        when(snsAsyncClient.publish(any(PublishRequest.class))).thenReturn(future);

        snsDomainEventPublisher.publishPaymentFailedEvent(event);

        verify(snsAsyncClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void publishPaymentFailedEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {
        PaymentFailedEvent event = new PaymentFailedEvent("ORDER-123", "PAY-123", "Error details", "detail", LocalDateTime.now());
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Error") {});

        snsDomainEventPublisher.publishPaymentFailedEvent(event);

        verify(snsAsyncClient, never()).publish(any(PublishRequest.class));
    }
}

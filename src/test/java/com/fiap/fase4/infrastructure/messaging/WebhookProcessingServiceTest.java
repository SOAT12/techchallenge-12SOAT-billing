package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookProcessingServiceTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookProcessingService webhookProcessingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webhookProcessingService, "queueUrl", "http://test-queue");
    }

    @Test
    void processNotificationAsync_shouldSendToSqs() throws Exception {
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", "123");
        String messageBody = "{\"resourceType\":\"payment\",\"resourceId\":\"123\"}";

        when(objectMapper.writeValueAsString(request)).thenReturn(messageBody);
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(CompletableFuture.completedFuture(null));

        webhookProcessingService.processNotificationAsync(request);

        verify(sqsAsyncClient).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void processNotificationAsync_shouldLogErrorWhenExceptionThrown() throws Exception {
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", "123");

        when(objectMapper.writeValueAsString(request)).thenThrow(new RuntimeException("Test Exception"));

        webhookProcessingService.processNotificationAsync(request);

        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
    }
}

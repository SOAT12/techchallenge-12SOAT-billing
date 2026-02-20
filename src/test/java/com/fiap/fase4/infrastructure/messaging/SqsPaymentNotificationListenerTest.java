package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.application.usecase.ProcessPaymentNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsPaymentNotificationListenerTest {

    @Mock
    private ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsPaymentNotificationListener listener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "queueUrl", "http://test-queue");
    }

    @Test
    void pollMessages_shouldProcessSuccessfully() throws Exception {
        Message message = Message.builder().body("test").receiptHandle("handle").build();
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder().messages(message).build();
        
        when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(receiveResponse));
        
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", "123");
        when(objectMapper.readValue("test", ProcessPaymentNotificationRequestDTO.class)).thenReturn(request);
        
        ProcessPaymentNotificationResponseDTO response = new ProcessPaymentNotificationResponseDTO(true, "APPROVED");
        when(processPaymentNotificationUseCase.execute(request)).thenReturn(response);
        
        when(sqsAsyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        listener.pollMessages();

        verify(processPaymentNotificationUseCase).execute(request);
        verify(sqsAsyncClient).deleteMessage(any(DeleteMessageRequest.class));
    }
}

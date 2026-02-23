package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessingService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.payment-notification-queue-url:http://localhost:4566/000000000000/payment-notifications-queue}")
    private String paymentNotificationQueueUrl;

    @Value("${app.sqs.payment-notification-queue-url:http://localhost:4566/000000000000/os-status-update-event}")
    private String osStatusQueueUrl;

    @Async
    public void processNotificationAsync(ProcessPaymentNotificationRequestDTO request) {
        try {
            log.info("Sending payment notification to SQS for resourceId: {}", request.resourceId());
            String messageBody = objectMapper.writeValueAsString(request);
            
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(paymentNotificationQueueUrl)
                    .messageBody(messageBody)
                    .build();
                    
            sqsAsyncClient.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            log.error("Error sending payment notification to SQS for resourceId: {}", request.resourceId(), ex);
        }
    }

    @Async
    public void osStatusAsync(ProcessPaymentNotificationRequestDTO request) {
        try {
            log.info("Sending payment notification to SQS for resourceId: {}", request.resourceId());
            String messageBody = objectMapper.writeValueAsString(request);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(osStatusQueueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsAsyncClient.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            log.error("Error sending payment notification to SQS for resourceId: {}", request.resourceId(), ex);
        }
    }
}

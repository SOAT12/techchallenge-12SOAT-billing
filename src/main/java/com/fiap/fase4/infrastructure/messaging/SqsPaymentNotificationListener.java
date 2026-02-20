package com.fiap.fase4.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.application.usecase.ProcessPaymentNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPaymentNotificationListener {

    private final ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.payment-notification-queue-url:http://localhost:4566/000000000000/payment-notifications-queue}")
    private String queueUrl;

    @Scheduled(fixedDelayString = "${app.sqs.polling-delay:5000}")
    public void pollMessages() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        sqsAsyncClient.receiveMessage(receiveRequest).whenComplete((response, exception) -> {
            if (exception != null) {
                log.error("Error receiving messages from SQS", exception);
                return;
            }

            if (response.messages() != null) {
                for (Message message : response.messages()) {
                    processMessage(message);
                }
            }
        });
    }

    private void processMessage(Message message) {
        try {
            ProcessPaymentNotificationRequestDTO request = objectMapper.readValue(message.body(), ProcessPaymentNotificationRequestDTO.class);
            log.info("Received message from SQS for payment notification: {}", request);
            
            ProcessPaymentNotificationResponseDTO result = processPaymentNotificationUseCase.execute(request);
            if (result.success()) {
                log.info("Payment {} processed successfully. Status: {}", request.resourceId(), result.updatedStatus());
                deleteMessage(message);
            } else {
                log.warn("Payment {} processing returned failure. Status: {}", request.resourceId(), result.updatedStatus());
            }
        } catch (Exception ex) {
            log.error("Error processing payment notification from SQS for message: {}", message.messageId(), ex);
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsAsyncClient.deleteMessage(deleteRequest).whenComplete((r, e) -> {
            if (e != null) {
                log.error("Failed to delete message: {}", message.messageId(), e);
            }
        });
    }
}

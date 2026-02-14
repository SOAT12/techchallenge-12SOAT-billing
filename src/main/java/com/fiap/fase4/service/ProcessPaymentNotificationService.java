package com.fiap.fase4.service;

import com.fiap.fase4.adapter.MercadoPagoAdapter;
import com.fiap.fase4.entity.PaymentEntity;
import com.fiap.fase4.exceptions.PaymentGatewayException;
import com.fiap.fase4.model.Payment;
import com.fiap.fase4.presenter.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.presenter.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.repository.PaymentRepository;
import com.fiap.fase4.utils.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class ProcessPaymentNotificationService {

    private final MercadoPagoAdapter mercadoPagoClient;
    private final PaymentRepository paymentRepository;

    /**
     * Processes a payment notification by fetching the payment details from the gateway
     * and updating the order status based on the payment status.
     *
     * @param input The request data containing the resource type and ID.
     * @return A response indicating success or failure, along with the updated payment status.
     */
    public ProcessPaymentNotificationResponseDTO processPaymentNotification(ProcessPaymentNotificationRequestDTO input) {
        log.info("Executing ProcessPaymentNotificationUseCase for resource type: {}, ID: {}",
                input.resourceType(), input.resourceId());

        ProcessPaymentNotificationResponseDTO INVALID_NOTIFICATION = validateInput(input);
        if (INVALID_NOTIFICATION != null) return INVALID_NOTIFICATION;

        String paymentId = input.resourceId();

        try {
            log.debug("Fetching payment details for ID: {}", paymentId);

            // 1. Fetch the actual payment details from Mercado Pago using the ID
            Payment paymentDetails = mercadoPagoClient.getPaymentDetails(paymentId);

            log.info("Fetched payment details for ID: {}. Status: {}", paymentId, paymentDetails.getStatus());

            // 2. Process the payment status (e.g., update order status in your database, delete cart if approved)
            PaymentStatus updatedStatus = getPaymentStatus(paymentDetails);

            // Update local database
            if (paymentDetails.getServiceOrderId() != null) {
                Optional<PaymentEntity> paymentEntityOptional = paymentRepository.findByServiceOrderId(paymentDetails.getServiceOrderId());
                if (paymentEntityOptional.isPresent()) {
                    PaymentEntity paymentEntity = paymentEntityOptional.get();
                    paymentEntity.setStatus(updatedStatus.name());
                    paymentEntity.setUpdatedAt(LocalDateTime.now());
                    if (paymentDetails.getPaymentMethod() != null) {
                        paymentEntity.setPaymentMethod(paymentDetails.getPaymentMethod().name());
                    }
                    paymentRepository.save(paymentEntity);
                    log.info("Updated payment status in database for serviceOrderId: {}", paymentDetails.getServiceOrderId());
                } else {
                    log.warn("Payment entity not found for serviceOrderId: {}", paymentDetails.getServiceOrderId());
                }
            } else {
                log.warn("Payment details missing serviceOrderId (external_reference). Cannot link to local payment entity.");
            }

            // 3. Return success and the updated status
            return new ProcessPaymentNotificationResponseDTO(true, updatedStatus.name());

        } catch (PaymentGatewayException e) {
            log.error("Error fetching payment details from gateway for payment ID {}: {}", paymentId, e.getMessage(), e);
            // Depending on the error, you might want to return success=false to trigger retries,
            // but be cautious of infinite retry loops.
            return new ProcessPaymentNotificationResponseDTO(false, "GATEWAY_ERROR");
        } catch (Exception e) {
            log.error("Unexpected error processing notification for payment ID {}: {}", paymentId, e.getMessage(), e);
            return new ProcessPaymentNotificationResponseDTO(false, "PROCESSING_ERROR");
        }
    }

    private static PaymentStatus getPaymentStatus(Payment paymentDetails) {
        return paymentDetails.getStatus() != null
                ? paymentDetails.getStatus() : PaymentStatus.UNKNOWN;
    }

    private static ProcessPaymentNotificationResponseDTO validateInput(ProcessPaymentNotificationRequestDTO input) {
        if (input == null || input.resourceId() == null || !"payment".equalsIgnoreCase(input.resourceType())) {
            log.warn("Invalid or non-payment notification received. Type: {}, ID: {}",
                    input.resourceType(), input.resourceId());
            // It's often better to return success (HTTP 200/201) and log the issue.
            return new ProcessPaymentNotificationResponseDTO(false, "INVALID_NOTIFICATION");
        }
        return null;
    }
}
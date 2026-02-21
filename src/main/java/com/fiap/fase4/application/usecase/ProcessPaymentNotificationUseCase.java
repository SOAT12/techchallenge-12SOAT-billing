package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.application.dto.ProcessPaymentNotificationResponseDTO;
import com.fiap.fase4.application.dto.events.PaymentApprovedEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import com.fiap.fase4.application.gateway.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentNotificationUseCase {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ProcessPaymentNotificationResponseDTO execute(ProcessPaymentNotificationRequestDTO input) {
        log.info("Executing ProcessPaymentNotificationUseCase for resource type: {}, ID: {}",
                input.resourceType(), input.resourceId());

        if (input.resourceId() == null || !"payment".equalsIgnoreCase(input.resourceType())) {
            return new ProcessPaymentNotificationResponseDTO(false, "INVALID_NOTIFICATION");
        }

        try {
            // Get payment details from Gateway
            Payment paymentDetails = paymentGateway.getPaymentDetails(input.resourceId());

            if (paymentDetails == null) {
                return new ProcessPaymentNotificationResponseDTO(false, "PAYMENT_NOT_FOUND_IN_GATEWAY");
            }

            // Find existing payment in DB by serviceOrderId (external reference)
            if (paymentDetails.getServiceOrderId() != null) {
                Optional<Payment> paymentOptional = paymentRepository.findByServiceOrderId(paymentDetails.getServiceOrderId());
                
                if (paymentOptional.isPresent()) {
                    Payment payment = paymentOptional.get();
                    payment.setPaymentId(input.resourceId());
                    payment.setStatus(paymentDetails.getStatus());
                    payment.setStatusDetail(paymentDetails.getStatusDetail());
                    payment.setPaymentMethod(paymentDetails.getPaymentMethod());
                    payment.setUpdatedAt(LocalDateTime.now());
                    
                    paymentRepository.save(payment);
                    
                    publishPaymentEvent(payment);
                    
                    return new ProcessPaymentNotificationResponseDTO(true, payment.getStatus().name());
                } else {
                    log.warn("Payment not found for order: {}", paymentDetails.getServiceOrderId());
                    return new ProcessPaymentNotificationResponseDTO(false, "PAYMENT_NOT_FOUND_LOCALLY");
                }
            }
            
            return new ProcessPaymentNotificationResponseDTO(false, "MISSING_SERVICE_ORDER_ID");

        } catch (Exception e) {
            log.error("Error processing notification", e);
            return new ProcessPaymentNotificationResponseDTO(false, "ERROR: " + e.getMessage());
        }
    }
    
    private void publishPaymentEvent(Payment payment) {
        if (PaymentStatus.APPROVED.equals(payment.getStatus())) {
            PaymentApprovedEvent event = new PaymentApprovedEvent(
                    payment.getServiceOrderId(),
                    payment.getPaymentId(),
                    payment.getPreferenceId(),
                    payment.getAmount(),
                    payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "UNKNOWN",
                    LocalDateTime.now()
            );
            domainEventPublisher.publishPaymentApprovedEvent(event);
        } else if (PaymentStatus.REJECTED.equals(payment.getStatus()) || PaymentStatus.CANCELLED.equals(payment.getStatus())) {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    payment.getServiceOrderId(),
                    payment.getPaymentId(),
                    "Payment was " + payment.getStatus().name(),
                    payment.getStatusDetail(),
                    LocalDateTime.now()
            );
            domainEventPublisher.publishPaymentFailedEvent(event);
        }
    }
}

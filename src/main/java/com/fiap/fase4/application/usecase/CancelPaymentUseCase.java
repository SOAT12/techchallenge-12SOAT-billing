package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.events.OrderCancelledEvent;
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
public class CancelPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final DomainEventPublisher domainEventPublisher;

    public void execute(OrderCancelledEvent event) {
        log.info("Executing CancelPaymentUseCase for order ID: {}", event.orderId());

        try {
            Optional<Payment> paymentOptional = paymentRepository.findByServiceOrderId(event.orderId());

            if (paymentOptional.isEmpty()) {
                log.warn("Cannot cancel payment. Payment not found locally for order: {}", event.orderId());
                return;
            }

            Payment payment = paymentOptional.get();

            // In an auto shop context, labor and items already applied cannot be "undone".
            // If payment was already approved, we mark it as needing manual intervention
            // instead of attempting an automatic refund.
            if (PaymentStatus.APPROVED.equals(payment.getStatus())) {
                boolean cancelled = paymentGateway.cancelPayment(payment.getId());
                if (cancelled) {
                    log.warn("Order cancellation requested for an ALREADY APPROVED payment. Order ID: {}. Manual intervention required.", event.orderId());
                    payment.setStatus(PaymentStatus.CANCELLED);
                    payment.setStatusDetail("Order cancelled after payment approval - manual resolution required for services/items");
                }
            } else {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setStatusDetail("Order cancelled before payment approval: " + event.reason());
                log.info("Payment marked as CANCELLED for order: {}", event.orderId());
            }

            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Publish a PaymentFailedEvent so any downstream listeners know the SAGA is rolling back
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    payment.getServiceOrderId(),
                    payment.getId(),
                    "Order was cancelled: " + event.reason(),
                    payment.getStatusDetail(),
                    LocalDateTime.now()
            );
            domainEventPublisher.publishPaymentFailedEvent(failedEvent);

            log.info("Payment cancellation/refund process completed for order: {}", event.orderId());

        } catch (Exception e) {
            log.error("Error processing payment cancellation for order ID: {}", event.orderId(), e);
        }
    }
}

package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.events.OrderCancelledEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import com.fiap.fase4.application.gateway.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private CancelPaymentUseCase useCase;

    @Test
    void execute_ShouldRefundAndPublishEvent_WhenPaymentIsApproved() {
        OrderCancelledEvent event = new OrderCancelledEvent("ORDER-123", "Out of stock", LocalDateTime.now());
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .preferenceId("987654")
                .status(PaymentStatus.APPROVED)
                .build();

        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.of(payment));
        when(paymentGateway.refundPayment("987654")).thenReturn(true);

        useCase.execute(event);

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(domainEventPublisher).publishPaymentFailedEvent(any(PaymentFailedEvent.class));
    }

    @Test
    void execute_ShouldCancelAndPublishEvent_WhenPaymentIsPending() {
        OrderCancelledEvent event = new OrderCancelledEvent("ORDER-123", "User cancelled", LocalDateTime.now());
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.of(payment));

        useCase.execute(event);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(paymentGateway, never()).refundPayment(any());
        verify(paymentRepository).save(payment);
        verify(domainEventPublisher).publishPaymentFailedEvent(any(PaymentFailedEvent.class));
    }

    @Test
    void execute_ShouldLogWarning_WhenPaymentNotFoundLocally() {
        OrderCancelledEvent event = new OrderCancelledEvent("ORDER-123", "Out of stock", LocalDateTime.now());

        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.empty());

        useCase.execute(event);

        verify(paymentGateway, never()).refundPayment(any());
        verify(paymentRepository, never()).save(any());
        verify(domainEventPublisher, never()).publishPaymentFailedEvent(any());
    }
}

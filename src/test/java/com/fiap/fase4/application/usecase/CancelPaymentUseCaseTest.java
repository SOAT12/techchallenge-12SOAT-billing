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
import org.mockito.ArgumentCaptor;
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
    void execute_ShouldMarkAsCancelledForManualResolution_WhenPaymentIsApproved() {
        OrderCancelledEvent event = new OrderCancelledEvent("ORDER-123", "Out of stock", LocalDateTime.now());
        
        // We use thenAnswer to return a NEW instance every time findByServiceOrderId is called
        // to avoid side-effects between the setup and the execution
        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenAnswer(inv -> Optional.of(
            Payment.builder()
                .id("PAY-123")
                .serviceOrderId("ORDER-123")
                .preferenceId("987654")
                .status(PaymentStatus.APPROVED)
                .build()
        ));

        when(paymentGateway.cancelPayment("PAY-123")).thenReturn(true);

        useCase.execute(event);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        assertEquals(PaymentStatus.CANCELLED, paymentCaptor.getValue().getStatus());
        verify(domainEventPublisher).publishPaymentFailedEvent(any(PaymentFailedEvent.class));
    }

    @Test
    void execute_ShouldCancelAndPublishEvent_WhenPaymentIsPending() {
        OrderCancelledEvent event = new OrderCancelledEvent("ORDER-123", "User cancelled", LocalDateTime.now());
        
        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenAnswer(inv -> Optional.of(
            Payment.builder()
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.PENDING)
                .build()
        ));

        useCase.execute(event);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        assertEquals(PaymentStatus.CANCELLED, paymentCaptor.getValue().getStatus());
        verify(paymentGateway, never()).refundPayment(any());
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

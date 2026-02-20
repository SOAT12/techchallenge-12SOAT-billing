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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentNotificationUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private ProcessPaymentNotificationUseCase useCase;

    @Test
    void execute_ShouldUpdatePaymentAndPublishApprovedEvent_WhenStatusIsApproved() {
        // Arrange
        String paymentId = "PAY-123";
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", paymentId);

        Payment paymentDetails = Payment.builder()
                .preferenceId("PREF-123")
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.APPROVED)
                .build();

        Payment existingPayment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentGateway.getPaymentDetails(paymentId)).thenReturn(paymentDetails);
        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.of(existingPayment));

        // Act
        ProcessPaymentNotificationResponseDTO response = useCase.execute(request);

        // Assert
        assertTrue(response.success());
        assertEquals("APPROVED", response.updatedStatus());
        assertEquals(PaymentStatus.APPROVED, existingPayment.getStatus());

        verify(paymentRepository).save(existingPayment);
        verify(domainEventPublisher).publishPaymentApprovedEvent(any(PaymentApprovedEvent.class));
        verify(domainEventPublisher, never()).publishPaymentFailedEvent(any());
    }

    @Test
    void execute_ShouldUpdatePaymentAndPublishFailedEvent_WhenStatusIsRejected() {
        // Arrange
        String paymentId = "PAY-123";
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", paymentId);

        Payment paymentDetails = Payment.builder()
                .preferenceId("PREF-123")
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.REJECTED)
                .statusDetail("insufficient_amount")
                .build();

        Payment existingPayment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentGateway.getPaymentDetails(paymentId)).thenReturn(paymentDetails);
        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.of(existingPayment));

        // Act
        ProcessPaymentNotificationResponseDTO response = useCase.execute(request);

        // Assert
        assertTrue(response.success());
        assertEquals("REJECTED", response.updatedStatus());
        assertEquals(PaymentStatus.REJECTED, existingPayment.getStatus());

        verify(paymentRepository).save(existingPayment);
        verify(domainEventPublisher).publishPaymentFailedEvent(any(PaymentFailedEvent.class));
        verify(domainEventPublisher, never()).publishPaymentApprovedEvent(any());
    }

    @Test
    void execute_ShouldReturnFalse_WhenPaymentNotFoundInGateway() {
        // Arrange
        String paymentId = "PAY-123";
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", paymentId);

        when(paymentGateway.getPaymentDetails(paymentId)).thenReturn(null);

        // Act
        ProcessPaymentNotificationResponseDTO response = useCase.execute(request);

        // Assert
        assertFalse(response.success());
        assertEquals("PAYMENT_NOT_FOUND_IN_GATEWAY", response.updatedStatus());
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void execute_ShouldReturnFalse_WhenPaymentNotFoundLocally() {
        // Arrange
        String paymentId = "PAY-123";
        ProcessPaymentNotificationRequestDTO request = new ProcessPaymentNotificationRequestDTO("payment", paymentId);

        Payment paymentDetails = Payment.builder()
                .serviceOrderId("ORDER-123")
                .status(PaymentStatus.APPROVED)
                .build();

        when(paymentGateway.getPaymentDetails(paymentId)).thenReturn(paymentDetails);
        when(paymentRepository.findByServiceOrderId("ORDER-123")).thenReturn(Optional.empty());

        // Act
        ProcessPaymentNotificationResponseDTO response = useCase.execute(request);

        // Assert
        assertFalse(response.success());
        assertEquals("PAYMENT_NOT_FOUND_LOCALLY", response.updatedStatus());
        verifyNoInteractions(domainEventPublisher);
    }
}

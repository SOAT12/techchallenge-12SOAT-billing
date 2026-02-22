package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.PaymentStatusResponseDTO;
import com.fiap.fase4.domain.entity.Payer;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import com.fiap.fase4.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPaymentStatusUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentStatusUseCase useCase;

    @Test
    void getByServiceOrderId_ShouldReturnResponse_WhenPaymentExists() {
        String serviceOrderId = "ORDER-123";
        Payment payment = createTestPayment(serviceOrderId, "PREF-456");

        when(paymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.of(payment));

        PaymentStatusResponseDTO response = useCase.getByServiceOrderId(serviceOrderId);

        assertNotNull(response);
        assertEquals(serviceOrderId, response.getServiceOrderId());
        assertEquals("PREF-456", response.getPreferenceId());
        assertEquals("CUSTOMER-789", response.getCustomerId());
        assertEquals(PaymentStatus.APPROVED, response.getStatus());
    }

    @Test
    void getByServiceOrderId_ShouldThrowException_WhenPaymentDoesNotExist() {
        String serviceOrderId = "ORDER-123";
        when(paymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.getByServiceOrderId(serviceOrderId));
    }

    @Test
    void getByPreferenceId_ShouldReturnResponse_WhenPaymentExists() {
        String preferenceId = "PREF-456";
        Payment payment = createTestPayment("ORDER-123", preferenceId);

        when(paymentRepository.findByPreferenceId(preferenceId)).thenReturn(Optional.of(payment));

        PaymentStatusResponseDTO response = useCase.getByPreferenceId(preferenceId);

        assertNotNull(response);
        assertEquals("ORDER-123", response.getServiceOrderId());
        assertEquals(preferenceId, response.getPreferenceId());
        assertEquals(PaymentStatus.APPROVED, response.getStatus());
    }

    @Test
    void getByPreferenceId_ShouldThrowException_WhenPaymentDoesNotExist() {
        String preferenceId = "PREF-456";
        when(paymentRepository.findByPreferenceId(preferenceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.getByPreferenceId(preferenceId));
    }

    private Payment createTestPayment(String serviceOrderId, String preferenceId) {
        return Payment.builder()
                .serviceOrderId(serviceOrderId)
                .preferenceId(preferenceId)
                .status(PaymentStatus.APPROVED)
                .checkoutUrl("http://checkout.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .payer(Payer.builder()
                        .identification(Payer.Identification.builder()
                                .number("CUSTOMER-789")
                                .build())
                        .build())
                .build();
    }
}

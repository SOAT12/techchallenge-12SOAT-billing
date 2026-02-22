package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.PaymentStatusResponseDTO;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import com.fiap.fase4.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPaymentStatusUseCase {

    private final PaymentRepository paymentRepository;

    public PaymentStatusResponseDTO getByServiceOrderId(String serviceOrderId) {
        Payment payment = paymentRepository.findByServiceOrderId(serviceOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Service Order ID: " + serviceOrderId));
        return mapToDTO(payment);
    }

    public PaymentStatusResponseDTO getByPreferenceId(String preferenceId) {
        Payment payment = paymentRepository.findByPreferenceId(preferenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Preference ID: " + preferenceId));
        return mapToDTO(payment);
    }

    private PaymentStatusResponseDTO mapToDTO(Payment payment) {
        String customerId = null;
        if (payment.getPayer() != null && payment.getPayer().getIdentification() != null) {
            customerId = payment.getPayer().getIdentification().getNumber();
        }

        return PaymentStatusResponseDTO.builder()
                .preferenceId(payment.getPreferenceId())
                .serviceOrderId(payment.getServiceOrderId())
                .customerId(customerId)
                .checkoutUrl(payment.getCheckoutUrl())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

package com.fiap.fase4.infrastructure.db.mapper;

import com.fiap.fase4.domain.entity.Payer;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentMethod;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.infrastructure.db.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId()) // Map ID
                .preferenceId(payment.getPreferenceId())
                .serviceOrderId(payment.getServiceOrderId())
                .amount(payment.getAmount())
                // .customerId(...) // mapping logic if needed
                .customerEmail(payment.getPayer() != null ? payment.getPayer().getEmail() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public Payment toDomain(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId()) // Map ID
                .preferenceId(entity.getPreferenceId())
                .serviceOrderId(entity.getServiceOrderId())
                .amount(entity.getAmount())
                .status(entity.getStatus() != null ? PaymentStatus.valueOf(entity.getStatus()) : PaymentStatus.UNKNOWN)
                .paymentMethod(entity.getPaymentMethod() != null ? 
                        safeValueOfPaymentMethod(entity.getPaymentMethod()) : null)
                .payer(Payer.builder()
                        .email(entity.getCustomerEmail())
                        .build())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PaymentMethod safeValueOfPaymentMethod(String method) {
        try {
            return PaymentMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return PaymentMethod.OTHER;
        }
    }
}

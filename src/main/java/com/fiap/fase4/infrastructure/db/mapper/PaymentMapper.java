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
        String customerId = payment.getPayer() != null && payment.getPayer().getIdentification() != null
                ? payment.getPayer().getIdentification().getNumber()
                : null;
        return PaymentEntity.builder()
                .id(payment.getId())
                .preferenceId(payment.getPreferenceId())
                .paymentId(payment.getPaymentId())
                .checkoutUrl(payment.getCheckoutUrl())
                .serviceOrderId(payment.getServiceOrderId())
                .amount(payment.getAmount())
                .customerId(customerId)
                .customerEmail(payment.getPayer() != null ? payment.getPayer().getEmail() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .statusDetail(payment.getStatusDetail())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public Payment toDomain(PaymentEntity entity) {
        Payer.Identification identification = entity.getCustomerId() != null
                ? Payer.Identification.builder().number(entity.getCustomerId()).build()
                : null;
        return Payment.builder()
                .id(entity.getId())
                .preferenceId(entity.getPreferenceId())
                .paymentId(entity.getPaymentId())
                .checkoutUrl(entity.getCheckoutUrl())
                .serviceOrderId(entity.getServiceOrderId())
                .amount(entity.getAmount())
                .status(entity.getStatus() != null ? PaymentStatus.valueOf(entity.getStatus()) : PaymentStatus.UNKNOWN)
                .statusDetail(entity.getStatusDetail())
                .paymentMethod(entity.getPaymentMethod() != null ?
                        safeValueOfPaymentMethod(entity.getPaymentMethod()) : null)
                .payer(Payer.builder()
                        .email(entity.getCustomerEmail())
                        .identification(identification)
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

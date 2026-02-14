package com.fiap.fase4.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Payment {

    private String id;
    private String preferenceId;
    private String checkoutUrl;
    private String serviceOrderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String statusDetail;
    private PaymentMethod paymentMethod;
    private Payer payer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
        this.createdAt = LocalDateTime.now();
    }
}

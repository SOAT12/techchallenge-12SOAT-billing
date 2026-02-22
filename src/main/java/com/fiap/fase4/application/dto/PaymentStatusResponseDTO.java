package com.fiap.fase4.application.dto;

import com.fiap.fase4.domain.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusResponseDTO {
    private String preferenceId;
    private String serviceOrderId;
    private String customerId;
    private String checkoutUrl;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.fiap.fase4.infrastructure.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "billing")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity {

    @Id
    private String id;
    private String preferenceId;
    private String serviceOrderId;
    private String checkoutUrl;
    private BigDecimal amount;
    private String customerId;
    private String customerEmail;
    private String status;
    private String statusDetail;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

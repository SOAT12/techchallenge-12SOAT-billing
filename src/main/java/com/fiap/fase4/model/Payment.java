package com.fiap.fase4.model;

import com.fiap.fase4.utils.PaymentMethod;
import com.fiap.fase4.utils.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Payment {

    private String preferenceId;
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

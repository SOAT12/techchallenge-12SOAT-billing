package com.fiap.fase4.domain.entity;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentItem {
    private String id;
    private String itemName;
    private int quantity;
    private BigDecimal price;
}

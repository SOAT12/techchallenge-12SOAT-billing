package com.fiap.fase4.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentPreference {
    private String id;
    private String checkoutUrl;
}

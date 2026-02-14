package com.fiap.fase4.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentUrls {
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
    private String notificationUrl;
}

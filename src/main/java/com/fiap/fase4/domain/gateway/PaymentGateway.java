package com.fiap.fase4.domain.gateway;

import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentItem;
import com.fiap.fase4.domain.entity.PaymentPreference;
import com.fiap.fase4.domain.entity.PaymentUrls;

import java.util.List;

public interface PaymentGateway {
    PaymentPreference createPreference(Payment payment, List<PaymentItem> items, PaymentUrls urls);
    Payment getPaymentDetails(String paymentId);
    boolean refundPayment(String paymentId);
}

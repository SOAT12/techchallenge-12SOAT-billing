package com.fiap.fase4.domain.gateway;

import com.fiap.fase4.domain.entity.Payment;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByPreferenceId(String preferenceId);
    Optional<Payment> findByServiceOrderId(String serviceOrderId);
}

package com.fiap.fase4.infrastructure.gateway;

import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import com.fiap.fase4.infrastructure.db.entity.PaymentEntity;
import com.fiap.fase4.infrastructure.db.mapper.PaymentMapper;
import com.fiap.fase4.infrastructure.db.repository.SpringDataPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        PaymentEntity savedEntity = springDataPaymentRepository.save(entity);
        return paymentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findByPreferenceId(String preferenceId) {
        return springDataPaymentRepository.findByPreferenceId(preferenceId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByServiceOrderId(String serviceOrderId) {
        return springDataPaymentRepository.findByServiceOrderId(serviceOrderId)
                .map(paymentMapper::toDomain);
    }
}

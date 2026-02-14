package com.fiap.fase4.infrastructure.db.repository;

import com.fiap.fase4.infrastructure.db.entity.PaymentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPaymentRepository extends MongoRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByServiceOrderId(String serviceOrderId);
    Optional<PaymentEntity> findByPreferenceId(String preferenceId);
}

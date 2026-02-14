package com.fiap.fase4.repository;

import com.fiap.fase4.entity.PaymentEntity;
import com.fiap.fase4.model.Payment;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentEntity, String> {

    Optional<PaymentEntity> findByServiceOrderId(String serviceOrderId);

}

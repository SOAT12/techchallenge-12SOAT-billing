package com.fiap.fase4.infrastructure.db.mapper;

import com.fiap.fase4.domain.entity.Payer;
import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentMethod;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.infrastructure.db.entity.PaymentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    void shouldMapToEntitySuccessfully() {
        Payer payer = Payer.builder()
                .email("test@email.com")
                .identification(Payer.Identification.builder().number("123456789").build())
                .customerName("John Doe")
                .build();

        Payment payment = Payment.builder()
                .id("PAY-1")
                .preferenceId("PREF-1")
                .checkoutUrl("http://checkout.com")
                .serviceOrderId("ORDER-1")
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.APPROVED)
                .statusDetail("accredited")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .payer(payer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentEntity entity = paymentMapper.toEntity(payment);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(payment.getId());
        assertThat(entity.getPreferenceId()).isEqualTo(payment.getPreferenceId());
        assertThat(entity.getCheckoutUrl()).isEqualTo(payment.getCheckoutUrl());
        assertThat(entity.getServiceOrderId()).isEqualTo(payment.getServiceOrderId());
        assertThat(entity.getAmount()).isEqualTo(payment.getAmount());
        assertThat(entity.getCustomerId()).isEqualTo("123456789");
        assertThat(entity.getCustomerEmail()).isEqualTo("test@email.com");
        assertThat(entity.getStatus()).isEqualTo("APPROVED");
        assertThat(entity.getStatusDetail()).isEqualTo("accredited");
        assertThat(entity.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(entity.getCreatedAt()).isEqualTo(payment.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(payment.getUpdatedAt());
    }

    @Test
    void shouldMapToEntityWithNullValues() {
        Payment payment = Payment.builder().build();

        PaymentEntity entity = paymentMapper.toEntity(payment);

        assertThat(entity).isNotNull();
        assertThat(entity.getCustomerId()).isNull();
        assertThat(entity.getCustomerEmail()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getPaymentMethod()).isNull();
    }
    
    @Test
    void shouldMapToEntityWithPayerWithoutIdentification() {
        Payer payer = Payer.builder().email("test@email.com").build();
        Payment payment = Payment.builder().payer(payer).build();

        PaymentEntity entity = paymentMapper.toEntity(payment);

        assertThat(entity).isNotNull();
        assertThat(entity.getCustomerId()).isNull();
        assertThat(entity.getCustomerEmail()).isEqualTo("test@email.com");
    }

    @Test
    void shouldMapToDomainSuccessfully() {
        PaymentEntity entity = PaymentEntity.builder()
                .id("PAY-1")
                .preferenceId("PREF-1")
                .checkoutUrl("http://checkout.com")
                .serviceOrderId("ORDER-1")
                .amount(new BigDecimal("100.00"))
                .customerId("123456789")
                .customerEmail("test@email.com")
                .status("APPROVED")
                .statusDetail("accredited")
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment payment = paymentMapper.toDomain(entity);

        assertThat(payment).isNotNull();
        assertThat(payment.getId()).isEqualTo(entity.getId());
        assertThat(payment.getPreferenceId()).isEqualTo(entity.getPreferenceId());
        assertThat(payment.getCheckoutUrl()).isEqualTo(entity.getCheckoutUrl());
        assertThat(payment.getServiceOrderId()).isEqualTo(entity.getServiceOrderId());
        assertThat(payment.getAmount()).isEqualTo(entity.getAmount());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getStatusDetail()).isEqualTo(entity.getStatusDetail());
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(payment.getPayer()).isNotNull();
        assertThat(payment.getPayer().getEmail()).isEqualTo("test@email.com");
        assertThat(payment.getPayer().getIdentification()).isNotNull();
        assertThat(payment.getPayer().getIdentification().getNumber()).isEqualTo("123456789");
        assertThat(payment.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(payment.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void shouldMapToDomainWithNullValues() {
        PaymentEntity entity = PaymentEntity.builder().build();

        Payment payment = paymentMapper.toDomain(entity);

        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(payment.getPaymentMethod()).isNull();
        assertThat(payment.getPayer()).isNotNull();
        assertThat(payment.getPayer().getIdentification()).isNull();
        assertThat(payment.getPayer().getEmail()).isNull();
    }
    
    @Test
    void shouldMapToDomainWithInvalidPaymentMethod() {
        PaymentEntity entity = PaymentEntity.builder()
                .paymentMethod("INVALID_METHOD")
                .build();

        Payment payment = paymentMapper.toDomain(entity);

        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.OTHER);
    }
}

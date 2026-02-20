package com.fiap.fase4.infrastructure.gateway;

import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.infrastructure.db.entity.PaymentEntity;
import com.fiap.fase4.infrastructure.db.mapper.PaymentMapper;
import com.fiap.fase4.infrastructure.db.repository.SpringDataPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRepositoryImplTest {

    @Mock
    private SpringDataPaymentRepository springDataPaymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentRepositoryImpl paymentRepositoryImpl;

    @Test
    void shouldSavePaymentSuccessfully() {
        Payment payment = Payment.builder().id("PAY-1").build();
        PaymentEntity entity = PaymentEntity.builder().id("PAY-1").build();

        when(paymentMapper.toEntity(payment)).thenReturn(entity);
        when(springDataPaymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toDomain(entity)).thenReturn(payment);

        Payment savedPayment = paymentRepositoryImpl.save(payment);

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isEqualTo("PAY-1");

        verify(paymentMapper).toEntity(payment);
        verify(springDataPaymentRepository).save(entity);
        verify(paymentMapper).toDomain(entity);
    }

    @Test
    void shouldFindPaymentByPreferenceId() {
        String preferenceId = "PREF-1";
        PaymentEntity entity = PaymentEntity.builder().preferenceId(preferenceId).build();
        Payment payment = Payment.builder().preferenceId(preferenceId).build();

        when(springDataPaymentRepository.findByPreferenceId(preferenceId)).thenReturn(Optional.of(entity));
        when(paymentMapper.toDomain(entity)).thenReturn(payment);

        Optional<Payment> result = paymentRepositoryImpl.findByPreferenceId(preferenceId);

        assertThat(result).isPresent();
        assertThat(result.get().getPreferenceId()).isEqualTo(preferenceId);

        verify(springDataPaymentRepository).findByPreferenceId(preferenceId);
        verify(paymentMapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenFindByPreferenceIdNotFound() {
        String preferenceId = "PREF-1";

        when(springDataPaymentRepository.findByPreferenceId(preferenceId)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentRepositoryImpl.findByPreferenceId(preferenceId);

        assertThat(result).isEmpty();

        verify(springDataPaymentRepository).findByPreferenceId(preferenceId);
        verify(paymentMapper, never()).toDomain(any());
    }

    @Test
    void shouldFindPaymentByServiceOrderId() {
        String serviceOrderId = "ORDER-1";
        PaymentEntity entity = PaymentEntity.builder().serviceOrderId(serviceOrderId).build();
        Payment payment = Payment.builder().serviceOrderId(serviceOrderId).build();

        when(springDataPaymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.of(entity));
        when(paymentMapper.toDomain(entity)).thenReturn(payment);

        Optional<Payment> result = paymentRepositoryImpl.findByServiceOrderId(serviceOrderId);

        assertThat(result).isPresent();
        assertThat(result.get().getServiceOrderId()).isEqualTo(serviceOrderId);

        verify(springDataPaymentRepository).findByServiceOrderId(serviceOrderId);
        verify(paymentMapper).toDomain(entity);
    }
    
    @Test
    void shouldReturnEmptyWhenFindByServiceOrderIdNotFound() {
        String serviceOrderId = "ORDER-1";

        when(springDataPaymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentRepositoryImpl.findByServiceOrderId(serviceOrderId);

        assertThat(result).isEmpty();

        verify(springDataPaymentRepository).findByServiceOrderId(serviceOrderId);
        verify(paymentMapper, never()).toDomain(any());
    }
}

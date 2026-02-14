package com.fiap.fase4.service;

import com.fiap.fase4.adapter.MercadoPagoAdapter;
import com.fiap.fase4.entity.PaymentEntity;
import com.fiap.fase4.presenter.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.presenter.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.repository.PaymentRepository;
import com.fiap.fase4.utils.PaymentStatus;
import com.mercadopago.client.MercadoPagoClient;
import com.mercadopago.resources.preference.Preference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PaymentService {

    private final MercadoPagoAdapter mercadoPagoClient;

    private final PaymentRepository paymentRepository;

    public CreatePreferenceResponseDTO createMercadoPagoPaymentLink(CreatePreferenceRequestDTO request) {

        //TODO VALIDATE REQUEST INFO
        Preference preference = mercadoPagoClient.createPreference(request, request.getServiceOrderId());

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .preferenceId(preference.getId())
                .serviceOrderId(request.getServiceOrderId())
                .amount(request.getTotalAmount())
                .customerId(request.getCustomer() != null ? request.getCustomer().getCustomerId() : null)
                .customerEmail(request.getCustomer() != null ? request.getCustomer().getEmail() : null)
                .status(PaymentStatus.PENDING.name())
                .paymentMethod("UNKNOWN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentEntity save = paymentRepository.save(paymentEntity);

        return new CreatePreferenceResponseDTO(
                preference.getId(),
                preference.getInitPoint()
        );
    }
}

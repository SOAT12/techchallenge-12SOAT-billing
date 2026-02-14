package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.application.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.domain.entity.*;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatePaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;

    public CreatePreferenceResponseDTO execute(CreatePreferenceRequestDTO request) {
        // Map DTO to Domain Payer
        Payer payer = Payer.builder()
                .email(request.getCustomer().getEmail())
                .customerName(request.getCustomer().getCustomerName())
                .build();
        
        if (request.getCustomer().getDocument() != null) {
             payer.setIdentification(Payer.Identification.builder()
                 .number(request.getCustomer().getDocument())
                 .type("CPF") 
                 .build());
        }

        // Map Items
        List<PaymentItem> items = request.getItems().stream()
                .map(item -> PaymentItem.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Map Urls
        PaymentUrls urls = PaymentUrls.builder()
                .successUrl(request.getResponseUrls().getSuccessUrl())
                .failureUrl(request.getResponseUrls().getFailureUrl())
                .pendingUrl(request.getResponseUrls().getPendingUrl())
                .notificationUrl(request.getNotificationUrl())
                .build();

        // Create initial Payment domain entity
        Payment payment = Payment.builder()
                .serviceOrderId(request.getServiceOrderId())
                .amount(request.getTotalAmount())
                .payer(payer)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Call Gateway
        PaymentPreference preference = paymentGateway.createPreference(payment, items, urls);

        // Update Payment with preference info
        payment.setPreferenceId(preference.getId());

        // Save Payment
        paymentRepository.save(payment);

        return new CreatePreferenceResponseDTO(preference.getId(), preference.getCheckoutUrl());
    }
}

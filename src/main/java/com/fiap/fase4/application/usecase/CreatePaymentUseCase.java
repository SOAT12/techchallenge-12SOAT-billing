package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.application.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.application.dto.CustomerDTO;
import com.fiap.fase4.domain.entity.*;
import com.fiap.fase4.exceptions.GenericBadRequestException;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
                .identification(customerId(request.getCustomer()))
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

        //Map services
        if (request.getServices() != null) {
            items.addAll(request.getServices().stream()
                    .map(service -> PaymentItem.builder()
                            .id(service.getId())
                            .itemName(service.getServiceName())
                            .price(service.getPrice())
                            .quantity(service.getQuantity())
                            .build())
                    .toList());
        }


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
                .amount(validateAmount(request.getTotalAmount(), items))
                .payer(payer)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Call Gateway
        PaymentPreference preference = paymentGateway.createPreference(payment, items, urls);

        // Update Payment with preference info
        payment.setPreferenceId(preference.getId());
        payment.setCheckoutUrl(preference.getCheckoutUrl());

        // Save Payment
        paymentRepository.save(payment);

        return new CreatePreferenceResponseDTO(preference.getId(), preference.getCheckoutUrl());
    }

    private BigDecimal validateAmount(BigDecimal totalAmount, List<PaymentItem> items) {
        BigDecimal itemsTotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (itemsTotal.compareTo(totalAmount) != 0) {
            throw new GenericBadRequestException(
                    String.format("Items total (%s) does not match totalAmount (%s)",
                            itemsTotal, totalAmount));
        }
        return totalAmount;
    }

    private Payer.Identification customerId(CustomerDTO customer) {
        return Payer.Identification.builder()
                .number(customer.getCustomerId())
                .type(customer.getCustomerId().length() == 11 ? "CPF" : "CNPJ")
                .build();
    }
}

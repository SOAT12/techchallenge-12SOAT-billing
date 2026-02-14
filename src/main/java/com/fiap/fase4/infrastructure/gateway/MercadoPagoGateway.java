package com.fiap.fase4.infrastructure.gateway;

import com.fiap.fase4.domain.entity.Payment;
import com.fiap.fase4.domain.entity.PaymentItem;
import com.fiap.fase4.domain.entity.PaymentPreference;
import com.fiap.fase4.domain.entity.PaymentUrls;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.domain.entity.PaymentMethod;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.exceptions.PaymentGatewayException;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoGateway implements PaymentGateway {

    @Value("${MERCADO_PAGO_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado Pago SDK initialized successfully.");
    }

    @Override
    public PaymentPreference createPreference(Payment payment, List<PaymentItem> items, PaymentUrls urls) {
        log.info("Creating Mercado Pago preference for orderNumber: {}", payment.getServiceOrderId());
        try {
            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> mpItems = items.stream()
                    .map(item -> PreferenceItemRequest.builder()
                            .id(item.getId())
                            .title(item.getItemName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getPrice())
                            .build())
                    .collect(Collectors.toList());

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(payment.getPayer().getEmail())
                    .name(payment.getPayer().getCustomerName())
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(urls.getSuccessUrl())
                    .failure(urls.getFailureUrl())
                    .pending(urls.getPendingUrl())
                    .build();

            String actualNotificationUrl = urls.getNotificationUrl() != null && !urls.getNotificationUrl().isBlank()
                    ? urls.getNotificationUrl()
                    : notificationUrl;

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(mpItems)
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(actualNotificationUrl)
                    .externalReference(payment.getServiceOrderId())
                    .build();

            Preference preference = client.create(request);

            log.info("Mercado Pago preference created. ID: {}, InitPoint: {}", preference.getId(), preference.getInitPoint());

            return new PaymentPreference(preference.getId(), preference.getInitPoint());

        } catch (MPApiException e) {
            log.error("Mercado Pago API error: Status Code: {}, Response: {}", e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new PaymentGatewayException("Mercado Pago API error: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago SDK error: {}", e.getMessage(), e);
            throw new PaymentGatewayException("Mercado Pago SDK error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Mercado Pago preference: {}", e.getMessage(), e);
            throw new PaymentGatewayException("Unexpected error creating preference.", e);
        }
    }

    @Override
    public Payment getPaymentDetails(String paymentId) {
        log.info("Getting Mercado Pago payment details for payment ID: {}", paymentId);
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(paymentId));

            if (mpPayment == null) {
                log.warn("Payment not found in Mercado Pago for ID: {}", paymentId);
                throw new PaymentGatewayException("Payment not found with ID: " + paymentId);
            }

            PaymentStatus status;
            try {
                status = PaymentStatus.valueOf(mpPayment.getStatus().toUpperCase());
            } catch (Exception e) {
                status = PaymentStatus.UNKNOWN;
            }
            
            PaymentMethod method = mapMercadoPagoMethod(mpPayment.getPaymentMethodId());

            return Payment.builder()
                    .preferenceId(mpPayment.getId().toString())
                    .serviceOrderId(mpPayment.getExternalReference())
                    .amount(mpPayment.getTransactionAmount())
                    .paymentMethod(method)
                    .status(status)
                    .statusDetail(mpPayment.getStatusDetail())
                    .build();

        } catch (Exception e) {
             throw new PaymentGatewayException("Error fetching payment details: " + e.getMessage(), e);
        }
    }

    private PaymentMethod mapMercadoPagoMethod(String methodId) {
        if (methodId == null) return null;
        if (methodId.startsWith("pix")) return PaymentMethod.PIX;
        if (methodId.matches("visa|master|amex|elo|hipercard|diners|discover|aura|jcb")) return PaymentMethod.CREDIT_CARD;
        return PaymentMethod.OTHER;
    }
}

package com.fiap.fase4.adapter;

import com.fiap.fase4.model.Payer;
import com.fiap.fase4.presenter.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.presenter.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.exceptions.PaymentGatewayException;
import com.fiap.fase4.model.Payment;
import com.fiap.fase4.utils.PaymentMethod;
import com.fiap.fase4.utils.PaymentStatus;
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
public class MercadoPagoAdapter {

    @Value("${MERCADO_PAGO_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    /**
     * Initializes the Mercado Pago SDK with the provided access token.
     * This method is called after the bean is constructed to ensure the SDK is ready for use.
     */
    @PostConstruct
    public void init() {
        // Initialize Mercado Pago SDK with Access Token
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado Pago SDK initialized successfully.");
    }

    /**
     * Creates a payment preference in Mercado Pago.
     *
     * @param input        The request data for creating the preference.
     * @param orderNumber  The order number to link with the preference.
     * @return A DTO containing the created preference ID and redirect URL.
     * @throws PaymentGatewayException If there is an error during the creation process.
     */
    public Preference createPreference(CreatePreferenceRequestDTO input, String orderNumber) throws PaymentGatewayException {
        log.info("Creating Mercado Pago preference for orderNumber: {}", orderNumber);
        try {
            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = input.getItems().stream()
                    .map(itemInput -> PreferenceItemRequest.builder()
                            .id(itemInput.getId())
                            .title(itemInput.getItemName())
                            .quantity(itemInput.getQuantity())
                            .unitPrice(itemInput.getPrice())
                            .build())
                    .collect(Collectors.toList());

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(input.getCustomer().getEmail())
                    .name(input.getCustomer().getCustomerName())
                    // Add other payer details if available in input
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(input.getResponseUrls().getSuccessUrl())
                    .failure(input.getResponseUrls().getFailureUrl())
                    .pending(input.getResponseUrls().getPendingUrl())
                    .build();

            String actualNotificationUrl = input.getNotificationUrl() != null && !input.getNotificationUrl().isBlank()
                    ? input.getNotificationUrl()
                    : notificationUrl;

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(actualNotificationUrl)
                    .externalReference(orderNumber) // Link to your order ID
                    // Add expiration settings, excluded payment methods/types if needed
                    .build();

            // Create the preference using the Mercado Pago client
            Preference preference = client.create(request);

            log.info("Mercado Pago preference created. ID: {}, InitPoint: {}", preference.getId(), preference.getInitPoint());

            return preference;

        } catch (MPApiException e) {
            log.error("Mercado Pago API error creating preference for orderNumber {}: Status Code: {}, Response: {}",
                    orderNumber, e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new PaymentGatewayException("Mercado Pago API error: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago SDK error creating preference for orderNumber {}: {}", orderNumber, e.getMessage(), e);
            throw new PaymentGatewayException("Mercado Pago SDK error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Mercado Pago preference for orderNumber {}: {}", orderNumber, e.getMessage(), e);
            throw new PaymentGatewayException("Unexpected error creating preference.", e);
        }
    }

    /**
     * Retrieves payment details from Mercado Pago using the payment ID.
     *
     * @param paymentId The ID of the payment to retrieve.
     * @return A Payment object containing the payment details.
     * @throws PaymentGatewayException If there is an error retrieving the payment details.
     */
    public Payment getPaymentDetails(String paymentId) throws PaymentGatewayException {
        log.info("Getting Mercado Pago payment details for payment ID: {}", paymentId);
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(paymentId));

            if (mpPayment == null) {
                log.warn("Payment not found in Mercado Pago for ID: {}", paymentId);
                throw new PaymentGatewayException("Payment not found with ID: " + paymentId);
            }

            log.debug("Raw payment details from MP: {}", mpPayment);

            // Map Mercado Pago Payment resource to our domain Payment model
            PaymentStatus status;
            try {
                status = mapMercadoPagoStatus(PaymentStatus.valueOf(mpPayment.getStatus().toUpperCase()));
            } catch (IllegalArgumentException | NullPointerException e) {
                log.warn("Unknown payment status received from Mercado Pago: {}", mpPayment.getStatus());
                status = PaymentStatus.UNKNOWN;
            }
            
            PaymentMethod method = mapMercadoPagoMethod(mpPayment.getPaymentMethodId());

            Payer payer = null;
            if (mpPayment.getPayer() != null) {
                Payer.Identification identification = null;
                if (mpPayment.getPayer().getIdentification() != null) {
                    identification = Payer.Identification.builder()
                            .type(mpPayment.getPayer().getIdentification().getType())
                            .number(mpPayment.getPayer().getIdentification().getNumber())
                            .build();
                }
                payer = Payer.builder()
                        .email(mpPayment.getPayer().getEmail())
                        .customerName(mpPayment.getPayer().getFirstName())
                        .identification(identification)
                        .build();
            }

            return Payment.builder()
                    .preferenceId(mpPayment.getId().toString())
                    .serviceOrderId(mpPayment.getExternalReference()) // Assuming externalReference holds our order ID
                    .amount(mpPayment.getTransactionAmount())
                    .paymentMethod(method)
                    .status(status)
                    .statusDetail(mpPayment.getStatusDetail())
                    .payer(payer)
                    .build();

        } catch (NumberFormatException e) {
            log.error("Invalid payment ID format: {}", paymentId, e);
            throw new PaymentGatewayException("Invalid payment ID format.", e);
        } catch (MPApiException e) {
            log.error("Mercado Pago API error getting payment details for ID {}: Status Code: {}, Response: {}",
                    paymentId, e.getStatusCode(), e.getApiResponse().getContent(), e);
            // Handle specific errors like 404 Not Found
            if (e.getStatusCode() == 404) {
                throw new PaymentGatewayException("Payment not found with ID: " + paymentId, e);
            }
            throw new PaymentGatewayException("Mercado Pago API error: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago SDK error getting payment details for ID {}: {}", paymentId, e.getMessage(), e);
            throw new PaymentGatewayException("Mercado Pago SDK error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error getting Mercado Pago payment details for ID {}: {}", paymentId, e.getMessage(), e);
            throw new PaymentGatewayException("Unexpected error getting payment details.", e);
        }
    }

    // --- Private Helper Methods ---
    // These mappers should ideally be placed in a separate mapper class for better separation of concerns.

    private PaymentStatus mapMercadoPagoStatus(PaymentStatus status) {
        if (status == null) return PaymentStatus.UNKNOWN;

        return switch (status) {
            case APPROVED -> PaymentStatus.APPROVED;
            case PENDING -> PaymentStatus.PENDING;
            case AUTHORIZED -> PaymentStatus.AUTHORIZED;
            case IN_PROCESS -> PaymentStatus.IN_PROCESS;
            case IN_MEDIATION -> PaymentStatus.IN_MEDIATION;
            case REJECTED -> PaymentStatus.REJECTED;
            case CANCELLED -> PaymentStatus.CANCELLED;
            case REFUNDED -> PaymentStatus.REFUNDED;
            case CHARGED_BACK -> PaymentStatus.CHARGED_BACK;
            default -> PaymentStatus.UNKNOWN;
        };
    }

    private PaymentMethod mapMercadoPagoMethod(String methodId) {
        if (methodId == null) return null; // Or a default/unknown type
        // Basic mapping, can be expanded based on actual method IDs used
        if (methodId.startsWith("pix")) {
            return PaymentMethod.PIX;
        } else if (methodId.matches("visa|master|amex|elo|hipercard|diners|discover|aura|jcb")) {
            // This is a simplification, Mercado Pago has specific IDs like 'master', 'visa'
            return PaymentMethod.CREDIT_CARD;
        } else if (methodId.equals("bolbradesco") || methodId.equals("pec")) {
            // Example for Boleto or PEC (Lotérica)
            // return Payment.PaymentMethod.BOLETO; // If you add Boleto later
            return null;
        }
        // Add mappings for other payment methods if needed
        return null; // Or Payment.PaymentMethod.OTHER
    }
}

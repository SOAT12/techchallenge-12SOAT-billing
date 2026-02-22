package com.fiap.fase4.infrastructure.gateway;

import com.fiap.fase4.domain.entity.*;
import com.fiap.fase4.exceptions.PaymentGatewayException;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoGatewayTest {

    @InjectMocks
    private MercadoPagoGateway mercadoPagoGateway;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mercadoPagoGateway, "accessToken", "TEST_TOKEN");
        ReflectionTestUtils.setField(mercadoPagoGateway, "notificationUrl", "http://notification");
    }

    @Test
    void init_shouldSetAccessToken() {
        mercadoPagoGateway.init();
    }

    @Test
    void createPreference_shouldReturnPaymentPreference() throws MPException, MPApiException {
        // Arrange
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .payer(Payer.builder().email("test@test.com").customerName("Test User").build())
                .build();
        List<PaymentItem> items = Collections.singletonList(
                PaymentItem.builder().id("ITEM-1").itemName("Item").quantity(1).price(BigDecimal.TEN).build()
        );
        PaymentUrls urls = PaymentUrls.builder().successUrl("success").failureUrl("failure").pendingUrl("pending").build();

        Preference mockPreference = mock(Preference.class);
        when(mockPreference.getId()).thenReturn("PREF-123");
        when(mockPreference.getInitPoint()).thenReturn("http://initpoint");

        try (MockedConstruction<PreferenceClient> mocked = Mockito.mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    when(mock.create(any(PreferenceRequest.class))).thenReturn(mockPreference);
                })) {

            // Act
            PaymentPreference result = mercadoPagoGateway.createPreference(payment, items, urls);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("PREF-123");
            assertThat(result.getCheckoutUrl()).isEqualTo("http://initpoint");
        }
    }

    @Test
    void createPreference_shouldThrowPaymentGatewayExceptionOnMPApiException() throws MPException, MPApiException {
        // Arrange
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .payer(Payer.builder().email("test@test.com").customerName("Test User").build())
                .build();
        List<PaymentItem> items = Collections.singletonList(
                PaymentItem.builder().id("ITEM-1").itemName("Item").quantity(1).price(BigDecimal.TEN).build()
        );
        PaymentUrls urls = PaymentUrls.builder().successUrl("success").failureUrl("failure").pendingUrl("pending").build();

        MPApiException exception = mock(MPApiException.class);
        MPResponse response = mock(MPResponse.class);
        when(response.getContent()).thenReturn("Error content");
        when(exception.getApiResponse()).thenReturn(response);
        when(exception.getStatusCode()).thenReturn(400);

        try (MockedConstruction<PreferenceClient> mocked = Mockito.mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    when(mock.create(any(PreferenceRequest.class))).thenThrow(exception);
                })) {

            // Act & Assert
            assertThatThrownBy(() -> mercadoPagoGateway.createPreference(payment, items, urls))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("Mercado Pago API error");
        }
    }

    @Test
    void getPaymentDetails_shouldReturnPayment() throws MPException, MPApiException {
        // Arrange
        com.mercadopago.resources.payment.Payment mpPayment = mock(com.mercadopago.resources.payment.Payment.class);
        when(mpPayment.getId()).thenReturn(12345L);
        when(mpPayment.getExternalReference()).thenReturn("ORDER-123");
        when(mpPayment.getTransactionAmount()).thenReturn(BigDecimal.TEN);
        when(mpPayment.getStatus()).thenReturn("approved");
        when(mpPayment.getStatusDetail()).thenReturn("accredited");
        when(mpPayment.getPaymentMethodId()).thenReturn("pix");

        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(12345L)).thenReturn(mpPayment);
                })) {

            // Act
            Payment result = mercadoPagoGateway.getPaymentDetails("12345");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPreferenceId()).isEqualTo("12345");
            assertThat(result.getServiceOrderId()).isEqualTo("ORDER-123");
            assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(result.getStatusDetail()).isEqualTo("accredited");
            assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
        }
    }

    @Test
    void getPaymentDetails_shouldThrowExceptionIfNotFound() throws MPException, MPApiException {
        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(12345L)).thenReturn(null);
                })) {

            assertThatThrownBy(() -> mercadoPagoGateway.getPaymentDetails("12345"))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    @Test
    void createPreference_shouldThrowPaymentGatewayExceptionOnMPException() throws MPException, MPApiException {
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .payer(Payer.builder().email("test@test.com").customerName("Test User").build())
                .build();
        List<PaymentItem> items = Collections.singletonList(
                PaymentItem.builder().id("ITEM-1").itemName("Item").quantity(1).price(BigDecimal.TEN).build()
        );
        PaymentUrls urls = PaymentUrls.builder().successUrl("success").failureUrl("failure").pendingUrl("pending").build();

        MPException exception = new MPException("SDK error");

        try (MockedConstruction<PreferenceClient> mocked = Mockito.mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    when(mock.create(any(PreferenceRequest.class))).thenThrow(exception);
                })) {

            assertThatThrownBy(() -> mercadoPagoGateway.createPreference(payment, items, urls))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("Mercado Pago SDK error");
        }
    }

    @Test
    void createPreference_shouldThrowPaymentGatewayExceptionOnGenericException() throws MPException, MPApiException {
        Payment payment = Payment.builder()
                .serviceOrderId("ORDER-123")
                .payer(Payer.builder().email("test@test.com").customerName("Test User").build())
                .build();
        List<PaymentItem> items = Collections.singletonList(
                PaymentItem.builder().id("ITEM-1").itemName("Item").quantity(1).price(BigDecimal.TEN).build()
        );
        PaymentUrls urls = PaymentUrls.builder().successUrl("success").failureUrl("failure").pendingUrl("pending").build();

        RuntimeException exception = new RuntimeException("Unexpected error");

        try (MockedConstruction<PreferenceClient> mocked = Mockito.mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    when(mock.create(any(PreferenceRequest.class))).thenThrow(exception);
                })) {

            assertThatThrownBy(() -> mercadoPagoGateway.createPreference(payment, items, urls))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("Unexpected error creating preference");
        }
    }

    @Test
    void getPaymentDetails_shouldReturnUnknownStatusAndCreditCardMethod() throws MPException, MPApiException {
        com.mercadopago.resources.payment.Payment mpPayment = mock(com.mercadopago.resources.payment.Payment.class);
        when(mpPayment.getId()).thenReturn(12345L);
        when(mpPayment.getExternalReference()).thenReturn("ORDER-123");
        when(mpPayment.getTransactionAmount()).thenReturn(BigDecimal.TEN);
        when(mpPayment.getStatus()).thenReturn("invalid_status");
        when(mpPayment.getStatusDetail()).thenReturn("accredited");
        when(mpPayment.getPaymentMethodId()).thenReturn("visa");

        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(12345L)).thenReturn(mpPayment);
                })) {

            Payment result = mercadoPagoGateway.getPaymentDetails("12345");

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
            assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        }
    }

    @Test
    void getPaymentDetails_shouldReturnOtherMethod() throws MPException, MPApiException {
        com.mercadopago.resources.payment.Payment mpPayment = mock(com.mercadopago.resources.payment.Payment.class);
        when(mpPayment.getId()).thenReturn(12345L);
        when(mpPayment.getExternalReference()).thenReturn("ORDER-123");
        when(mpPayment.getTransactionAmount()).thenReturn(BigDecimal.TEN);
        when(mpPayment.getStatus()).thenReturn("approved");
        when(mpPayment.getStatusDetail()).thenReturn("accredited");
        when(mpPayment.getPaymentMethodId()).thenReturn("bol");

        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(12345L)).thenReturn(mpPayment);
                })) {

            Payment result = mercadoPagoGateway.getPaymentDetails("12345");

            assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.OTHER);
        }
    }

    @Test
    void refundPayment_shouldReturnTrueWhenSuccessful() throws MPException, MPApiException {
        com.mercadopago.resources.payment.PaymentRefund refund = mock(com.mercadopago.resources.payment.PaymentRefund.class);
        when(refund.getId()).thenReturn(111L);

        try (MockedConstruction<com.mercadopago.client.payment.PaymentRefundClient> mocked = Mockito.mockConstruction(com.mercadopago.client.payment.PaymentRefundClient.class,
                (mock, context) -> {
                    when(mock.refund(12345L)).thenReturn(refund);
                })) {

            boolean result = mercadoPagoGateway.refundPayment("12345");
            assertThat(result).isTrue();
        }
    }

    @Test
    void refundPayment_shouldReturnFalseOnNumberFormatException() {
        boolean result = mercadoPagoGateway.refundPayment("not-a-number");
        assertThat(result).isFalse();
    }

    @Test
    void refundPayment_shouldReturnFalseOnMPApiException() throws MPException, MPApiException {
        MPApiException exception = mock(MPApiException.class);
        MPResponse response = mock(MPResponse.class);
        when(response.getContent()).thenReturn("Error");
        when(exception.getApiResponse()).thenReturn(response);
        
        try (MockedConstruction<com.mercadopago.client.payment.PaymentRefundClient> mocked = Mockito.mockConstruction(com.mercadopago.client.payment.PaymentRefundClient.class,
                (mock, context) -> {
                    when(mock.refund(12345L)).thenThrow(exception);
                })) {

            boolean result = mercadoPagoGateway.refundPayment("12345");
            assertThat(result).isFalse();
        }
    }

    @Test
    void refundPayment_shouldReturnFalseOnException() throws MPException, MPApiException {
        RuntimeException exception = new RuntimeException("Error");
        
        try (MockedConstruction<com.mercadopago.client.payment.PaymentRefundClient> mocked = Mockito.mockConstruction(com.mercadopago.client.payment.PaymentRefundClient.class,
                (mock, context) -> {
                    when(mock.refund(12345L)).thenThrow(exception);
                })) {

            boolean result = mercadoPagoGateway.refundPayment("12345");
            assertThat(result).isFalse();
        }
    }

    @Test
    void cancelPayment_shouldReturnTrueWhenSuccessful() throws MPException, MPApiException {
        com.mercadopago.resources.payment.Payment cancel = mock(com.mercadopago.resources.payment.Payment.class);
        when(cancel.getId()).thenReturn(111L);

        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.cancel(12345L)).thenReturn(cancel);
                })) {

            boolean result = mercadoPagoGateway.cancelPayment("12345");
            assertThat(result).isTrue();
        }
    }

    @Test
    void cancelPayment_shouldReturnFalseOnNumberFormatException() {
        boolean result = mercadoPagoGateway.cancelPayment("not-a-number");
        assertThat(result).isFalse();
    }

    @Test
    void cancelPayment_shouldReturnFalseOnMPApiException() throws MPException, MPApiException {
        MPApiException exception = mock(MPApiException.class);
        MPResponse response = mock(MPResponse.class);
        when(response.getContent()).thenReturn("Error");
        when(exception.getApiResponse()).thenReturn(response);
        
        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.cancel(12345L)).thenThrow(exception);
                })) {

            boolean result = mercadoPagoGateway.cancelPayment("12345");
            assertThat(result).isFalse();
        }
    }

    @Test
    void cancelPayment_shouldReturnFalseOnException() throws MPException, MPApiException {
        RuntimeException exception = new RuntimeException("Error");
        
        try (MockedConstruction<PaymentClient> mocked = Mockito.mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.cancel(12345L)).thenThrow(exception);
                })) {

            boolean result = mercadoPagoGateway.cancelPayment("12345");
            assertThat(result).isFalse();
        }
    }
}
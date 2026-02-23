package com.fiap.fase4.infrastructure.controller.mapper;

import com.fiap.fase4.application.dto.ProcessPaymentNotificationRequestDTO;
import com.fiap.fase4.infrastructure.controller.dto.MercadoPagoNotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookPayloadMapperTest {

    private WebhookPayloadMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookPayloadMapper();
    }

    @Test
    void shouldReturnEmptyWhenPayloadIsNull() {
        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(null);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenDataIsNull() {
        MercadoPagoNotificationDTO payload = new MercadoPagoNotificationDTO();
        payload.setType("payment");

        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(payload);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenDataIdIsNull() {
        MercadoPagoNotificationDTO payload = new MercadoPagoNotificationDTO();
        payload.setType("payment");
        payload.setData(new MercadoPagoNotificationDTO.NotificationData());

        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(payload);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTypeIsNull() {
        MercadoPagoNotificationDTO payload = new MercadoPagoNotificationDTO();
        MercadoPagoNotificationDTO.NotificationData data = new MercadoPagoNotificationDTO.NotificationData();
        data.setId("12345");
        payload.setData(data);

        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(payload);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTypeIsNotPayment() {
        MercadoPagoNotificationDTO payload = new MercadoPagoNotificationDTO();
        payload.setType("merchant_order");
        MercadoPagoNotificationDTO.NotificationData data = new MercadoPagoNotificationDTO.NotificationData();
        data.setId("12345");
        payload.setData(data);

        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(payload);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnRequestWhenPayloadIsValid() {
        MercadoPagoNotificationDTO payload = new MercadoPagoNotificationDTO();
        payload.setType("payment");
        MercadoPagoNotificationDTO.NotificationData data = new MercadoPagoNotificationDTO.NotificationData();
        data.setId("12345");
        payload.setData(data);

        Optional<ProcessPaymentNotificationRequestDTO> result = mapper.toProcessPaymentRequest(payload);
        
        assertThat(result).isPresent();
        assertThat(result.get().resourceId()).isEqualTo("12345");
    }
}

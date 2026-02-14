package com.fiap.fase4.infrastructure.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the incoming webhook payload from Mercado Pago.
 * Kept in infrastructure as it reflects the external HTTP contract.
 */
@Data
@NoArgsConstructor
public class MercadoPagoNotificationDTO {

    private String action;

    @JsonProperty("api_version")
    private String apiVersion;

    private NotificationData data;

    @JsonProperty("date_created")
    private String dateCreated;

    private long id;

    @JsonProperty("live_mode")
    private boolean liveMode;

    private String type;

    @JsonProperty("user_id")
    private long userId;

    @Data
    @NoArgsConstructor
    public static class NotificationData {
        private String id;
    }
}

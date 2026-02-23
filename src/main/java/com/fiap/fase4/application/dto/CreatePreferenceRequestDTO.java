package com.fiap.fase4.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreatePreferenceRequestDTO {
    private String serviceOrderId;
    private CustomerDTO customer;
//    private List<ItemDTO> items;
//    private List<ServiceDTO> services;
    private BigDecimal totalAmount;
    private ResponseUrlsDTO responseUrls;
    private String notificationUrl;

    public CreatePreferenceRequestDTO() {}
}

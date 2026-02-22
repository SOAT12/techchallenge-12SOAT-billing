package com.fiap.fase4.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ServiceDTO {
    private String id;
    private String serviceName;
    private BigDecimal price;
    private int quantity;
}

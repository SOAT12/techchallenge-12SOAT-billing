package com.fiap.fase4.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ItemDTO {
    private String id;
    private String itemName;
    private int quantity;
    private BigDecimal price;
}

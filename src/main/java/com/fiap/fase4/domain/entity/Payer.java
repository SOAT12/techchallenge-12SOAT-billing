package com.fiap.fase4.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payer {
    private String email;
    private String customerName;
    private Identification identification;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Identification {
        private String type; // e.g., "CPF", "CNPJ"
        private String number;
    }
}

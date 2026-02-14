package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.application.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.application.usecase.CreatePaymentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;

    @PostMapping("/mercadopago")
    public ResponseEntity<CreatePreferenceResponseDTO> createMercadoPagoCheckoutLink(@RequestBody CreatePreferenceRequestDTO request) {
        CreatePreferenceResponseDTO response = createPaymentUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}

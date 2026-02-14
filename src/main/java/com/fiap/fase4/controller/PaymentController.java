package com.fiap.fase4.controller;

import com.fiap.fase4.presenter.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.presenter.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/mercadopago")
    public ResponseEntity<CreatePreferenceResponseDTO> createMercadoPagoCheckoutLink(@RequestBody CreatePreferenceRequestDTO request) {
        CreatePreferenceResponseDTO response = paymentService.createMercadoPagoPaymentLink(request);
        return ResponseEntity.ok(response);
    }
}

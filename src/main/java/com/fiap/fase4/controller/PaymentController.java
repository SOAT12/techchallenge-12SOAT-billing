package com.fiap.fase4.controller;

import com.fiap.fase4.dto.infinitepay.CreateLinkRequest;
import com.fiap.fase4.dto.infinitepay.CreateLinkResponse;
import com.fiap.fase4.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<CreateLinkResponse> createCheckoutLink(@RequestBody CreateLinkRequest request) {
        CreateLinkResponse response = paymentService.createPaymentLink(request);
        return ResponseEntity.ok(response);
    }
}

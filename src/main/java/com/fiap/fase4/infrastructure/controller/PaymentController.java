package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.CreatePreferenceRequestDTO;
import com.fiap.fase4.application.dto.CreatePreferenceResponseDTO;
import com.fiap.fase4.application.dto.PaymentStatusResponseDTO;
import com.fiap.fase4.application.usecase.CreatePaymentUseCase;
import com.fiap.fase4.application.usecase.GetPaymentStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Endpoints for creating and tracking payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentStatusUseCase getPaymentStatusUseCase;

    @PostMapping("/mercadopago")
    @Operation(summary = "Create Mercado Pago checkout link", description = "Generates a preference ID and a checkout URL for a customer order")
    public ResponseEntity<CreatePreferenceResponseDTO> createMercadoPagoCheckoutLink(@RequestBody CreatePreferenceRequestDTO request) {
        CreatePreferenceResponseDTO response = createPaymentUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{serviceOrderId}")
    @Operation(summary = "Get status by Service Order ID", description = "Retrieves the payment status using the external service order reference")
    public ResponseEntity<PaymentStatusResponseDTO> getStatusByServiceOrderId(@PathVariable String serviceOrderId) {
        PaymentStatusResponseDTO response = getPaymentStatusUseCase.getByServiceOrderId(serviceOrderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preference/{preferenceId}")
    @Operation(summary = "Get status by Preference ID", description = "Retrieves the payment status using the Mercado Pago preference ID")
    public ResponseEntity<PaymentStatusResponseDTO> getStatusByPreferenceId(@PathVariable String preferenceId) {
        PaymentStatusResponseDTO response = getPaymentStatusUseCase.getByPreferenceId(preferenceId);
        return ResponseEntity.ok(response);
    }
}

package com.fiap.fase4.infrastructure.controller;

import com.fiap.fase4.application.dto.PaymentStatusResponseDTO;
import com.fiap.fase4.application.usecase.GetPaymentStatusUseCase;
import com.fiap.fase4.domain.entity.PaymentStatus;
import com.fiap.fase4.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private GetPaymentStatusUseCase getPaymentStatusUseCase;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void getStatusByServiceOrderId_ShouldReturn200_WhenFound() {
        String orderId = "123";
        PaymentStatusResponseDTO responseDTO = PaymentStatusResponseDTO.builder()
                .serviceOrderId(orderId)
                .status(PaymentStatus.APPROVED)
                .build();

        when(getPaymentStatusUseCase.getByServiceOrderId(orderId)).thenReturn(responseDTO);

        ResponseEntity<PaymentStatusResponseDTO> response = paymentController.getStatusByServiceOrderId(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderId, response.getBody().getServiceOrderId());
    }

    @Test
    void getStatusByServiceOrderId_ShouldThrowException_WhenNotFound() {
        String orderId = "999";
        when(getPaymentStatusUseCase.getByServiceOrderId(orderId))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> paymentController.getStatusByServiceOrderId(orderId));
    }

    @Test
    void getStatusByPreferenceId_ShouldReturn200_WhenFound() {
        String prefId = "pref-123";
        PaymentStatusResponseDTO responseDTO = PaymentStatusResponseDTO.builder()
                .preferenceId(prefId)
                .status(PaymentStatus.PENDING)
                .build();

        when(getPaymentStatusUseCase.getByPreferenceId(prefId)).thenReturn(responseDTO);

        ResponseEntity<PaymentStatusResponseDTO> response = paymentController.getStatusByPreferenceId(prefId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(prefId, response.getBody().getPreferenceId());
    }
}

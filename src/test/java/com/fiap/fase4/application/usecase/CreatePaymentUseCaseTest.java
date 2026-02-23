package com.fiap.fase4.application.usecase;

import com.fiap.fase4.application.dto.*;
import com.fiap.fase4.domain.entity.*;
import com.fiap.fase4.domain.gateway.PaymentGateway;
import com.fiap.fase4.domain.gateway.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private CreatePaymentUseCase createPaymentUseCase;

    private CreatePreferenceRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new CreatePreferenceRequestDTO();
        request.setServiceOrderId("ORDER-123");
        request.setTotalAmount(new BigDecimal("100.00"));

        CustomerDTO customer = new CustomerDTO();
        customer.setCustomerId("CUST-123");
        customer.setEmail("test@test.com");
        customer.setCustomerName("Test User");
        request.setCustomer(customer);

        ItemDTO item = new ItemDTO("ITEM-1", "Test Item", 1, new BigDecimal("100.00"));
//        request.setItems(Collections.singletonList(item));

        ResponseUrlsDTO urls = new ResponseUrlsDTO();
        urls.setSuccessUrl("http://success");
        urls.setFailureUrl("http://failure");
        urls.setPendingUrl("http://pending");
        request.setResponseUrls(urls);
    }

    @Test
    void execute_ShouldCreatePaymentAndReturnResponse() {
        // Arrange
        PaymentPreference preference = PaymentPreference.builder()
                .id("PREF-123")
                .checkoutUrl("http://checkout")
                .build();

        when(paymentGateway.createPreference(any(Payment.class)))
                .thenReturn(preference);

        // Act
        CreatePreferenceResponseDTO response = createPaymentUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals("PREF-123", response.getId());
        assertEquals("http://checkout", response.getCheckoutUrl());

        verify(paymentGateway).createPreference(any(Payment.class));
        verify(paymentRepository).save(any(Payment.class));
    }
}

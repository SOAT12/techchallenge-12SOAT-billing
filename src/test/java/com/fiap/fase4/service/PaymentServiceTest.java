//package com.fiap.fase4.service;
//
//import com.fiap.fase4.presenter.dto.CreatePreferenceRequestDTO;
//import com.fiap.fase4.presenter.dto.CreatePreferenceResponseDTO;
//import com.fiap.fase4.presenter.dto.ItemDTO;
//import com.fiap.fase4.model.Payment;
//import com.fiap.fase4.repository.PaymentRepository;
//import com.mercadopago.client.preference.PreferenceClient;
//import com.mercadopago.client.preference.PreferenceRequest;
//import com.mercadopago.resources.preference.Preference;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpEntity;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class PaymentServiceTest {
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Spy
//    @InjectMocks
//    private PaymentService paymentService;
//
//    @Test
//    public void testCreatePaymentLink() {
//        // Setup configuration values
//        ReflectionTestUtils.setField(paymentService, "handle", "test_handle");
//        ReflectionTestUtils.setField(paymentService, "apiUrl", "http://test-api.com");
//
//        // Prepare request
//        CreatePreferenceRequestDTO request = new CreatePreferenceRequestDTO();
//        ItemDTO item = new ItemDTO("Item 1", 1, 1000);
//        request.setItems(Collections.singletonList(item));
//
//        // Prepare mocked response
//        CreatePreferenceResponseDTO expectedResponse = new CreatePreferenceResponseDTO();
//        expectedResponse.setLink("http://checkout.link");
//
//        when(restTemplate.postForObject(eq("http://test-api.com"), any(HttpEntity.class), eq(CreatePreferenceResponseDTO.class)))
//                .thenReturn(expectedResponse);
//
//        // Execute service
////        CreatePreferenceResponseDTO actualResponse = paymentService.createPaymentLink(request);
//
//        // Verify
////        assertEquals("http://checkout.link", actualResponse.getLink());
//        assertEquals("test_handle", request.getHandle());
//        verify(restTemplate).postForObject(eq("http://test-api.com"), any(HttpEntity.class), eq(CreatePreferenceResponseDTO.class));
//    }
//
//    @Test
//    public void testCreateMercadoPagoPaymentLink() throws Exception {
//        // Prepare request
//        CreatePreferenceRequestDTO request = new CreatePreferenceRequestDTO();
//        request.setRedirect_url("http://redirect.url");
//        request.setWebhook_url("http://webhook.url");
//        ItemDTO item = new ItemDTO("Item 1", 1, 1000); // 1000 cents = 10.00
//        request.setItems(Collections.singletonList(item));
//
//        // Mock Mercado Pago objects
//        PreferenceClient mockClient = mock(PreferenceClient.class);
//        Preference mockPreference = mock(Preference.class);
//
//        // Setup spy to return mock client
//        doReturn(mockClient).when(paymentService).getPreferenceClient();
//
//        // Setup mock client behavior
//        when(mockClient.create(any(PreferenceRequest.class))).thenReturn(mockPreference);
//        when(mockPreference.getId()).thenReturn("pref_123");
//        when(mockPreference.getInitPoint()).thenReturn("http://mercadopago.checkout");
//
//        // Execute service
//        CreatePreferenceResponseDTO response = paymentService.createMercadoPagoPaymentLink(request);
//
//        // Verify
//        assertEquals("pref_123", response.getId());
//        assertEquals("http://mercadopago.checkout", response.getLink());
//        assertEquals("http://mercadopago.checkout", response.getCheckoutUrl());
//        assertEquals("CREATED", response.getStatus());
//
//        verify(mockClient).create(any(PreferenceRequest.class));
//        verify(paymentRepository).save(any(Payment.class));
//    }
//}

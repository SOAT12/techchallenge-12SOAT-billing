package com.fiap.fase4.service;

import com.fiap.fase4.dto.infinitepay.CreateLinkRequest;
import com.fiap.fase4.dto.infinitepay.CreateLinkResponse;
import com.fiap.fase4.dto.infinitepay.LinkItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void testCreatePaymentLink() {
        // Setup configuration values
        ReflectionTestUtils.setField(paymentService, "handle", "test_handle");
        ReflectionTestUtils.setField(paymentService, "apiUrl", "http://test-api.com");

        // Prepare request
        CreateLinkRequest request = new CreateLinkRequest();
        LinkItem item = new LinkItem("Item 1", 1, 1000);
        request.setItems(Collections.singletonList(item));

        // Prepare mocked response
        CreateLinkResponse expectedResponse = new CreateLinkResponse();
        expectedResponse.setLink("http://checkout.link");

        when(restTemplate.postForObject(eq("http://test-api.com"), any(HttpEntity.class), eq(CreateLinkResponse.class)))
                .thenReturn(expectedResponse);

        // Execute service
        CreateLinkResponse actualResponse = paymentService.createPaymentLink(request);

        // Verify
        assertEquals("http://checkout.link", actualResponse.getLink());
        assertEquals("test_handle", request.getHandle());
        verify(restTemplate).postForObject(eq("http://test-api.com"), any(HttpEntity.class), eq(CreateLinkResponse.class));
    }
}

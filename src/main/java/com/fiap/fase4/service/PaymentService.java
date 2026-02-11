package com.fiap.fase4.service;

import com.fiap.fase4.dto.infinitepay.CreateLinkRequest;
import com.fiap.fase4.dto.infinitepay.CreateLinkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

    @Value("${infinitepay.api.url:https://api.infinitepay.io/invoices/public/checkout/links}")
    private String apiUrl;

    @Value("${infinitepay.handle}")
    private String handle;

    @Autowired
    private RestTemplate restTemplate;

    public CreateLinkResponse createPaymentLink(CreateLinkRequest request) {
        request.setHandle(handle);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateLinkRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(apiUrl, entity, CreateLinkResponse.class);
    }
}

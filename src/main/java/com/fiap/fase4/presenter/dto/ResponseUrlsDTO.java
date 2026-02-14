package com.fiap.fase4.presenter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUrlsDTO {

    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
}

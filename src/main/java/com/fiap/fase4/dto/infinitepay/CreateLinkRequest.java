package com.fiap.fase4.dto.infinitepay;

import java.util.List;

public class CreateLinkRequest {
    private String handle;
    private List<LinkItem> items;
    private String order_nsu;
    private String redirect_url;
    private String webhook_url;
    private Object customer; // For now, simple object
    private Object address; // For now, simple object

    public CreateLinkRequest() {}

    public CreateLinkRequest(String handle, List<LinkItem> items) {
        this.handle = handle;
        this.items = items;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public List<LinkItem> getItems() {
        return items;
    }

    public void setItems(List<LinkItem> items) {
        this.items = items;
    }

    public String getOrder_nsu() {
        return order_nsu;
    }

    public void setOrder_nsu(String order_nsu) {
        this.order_nsu = order_nsu;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public String getWebhook_url() {
        return webhook_url;
    }

    public void setWebhook_url(String webhook_url) {
        this.webhook_url = webhook_url;
    }

    public Object getCustomer() {
        return customer;
    }

    public void setCustomer(Object customer) {
        this.customer = customer;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }
}

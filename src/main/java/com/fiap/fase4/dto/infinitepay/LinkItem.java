package com.fiap.fase4.dto.infinitepay;

public class LinkItem {
    private String description;
    private int quantity;
    private int price; // In cents

    public LinkItem() {}

    public LinkItem(String description, int quantity, int price) {
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

package ru.kotov.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReservationRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotBlank(message = "sku is required")
    @Size(max = 64, message = "sku must not exceed 64 characters")
    private String sku;

    @NotBlank(message = "quantity is required")
    @DecimalMin(value = "1", message = "quantity must be at least 1")
    private Integer quantity;

    public ReservationRequest() {
    }

    public ReservationRequest(String orderId, String sku, Integer quantity) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

package ru.kotov.orders.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class OrderRequest {

    @NotBlank(message = "orderNumber is required")
    @Size(max = 64, message = "orderNumber must not exceed 64 characters")
    private String orderNumber;

    @NotBlank(message = "customerName is required")
    @Size(max = 255, message = "customerName must not exceed 255 characters")
    private String customerName;

    @NotBlank(message = "customerEmail is required")
    @Email(message = "customerEmail must be a valid email")
    @Size(max = 255, message = "customerEmail must not exceed 255 characters")
    private String customerEmail;

    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than 0")
    private BigDecimal totalAmount;

    // Getters and setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

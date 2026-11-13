package ru.kotov.orders.dto;

import java.math.BigDecimal;

public class OrderCreatedEvent {

    private Long orderId;
    private String orderNumber;
    private String customerEmail;
    private BigDecimal totalAmount;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, String orderNumber, String customerEmail, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

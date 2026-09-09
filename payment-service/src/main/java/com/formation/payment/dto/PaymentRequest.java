package com.formation.payment.dto;

import com.formation.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    private String bookingReference;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be >= 0")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    private String cardLastFour;

    public PaymentRequest() {
    }

    // Getters / Setters

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }
}
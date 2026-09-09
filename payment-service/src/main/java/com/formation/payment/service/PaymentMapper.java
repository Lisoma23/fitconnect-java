package com.fitconnect.paymentservice.service;

import com.fitconnect.paymentservice.dto.PaymentRequest;
import com.fitconnect.paymentservice.dto.PaymentResponse;
import com.fitconnect.paymentservice.model.Payment;
import com.fitconnect.paymentservice.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setBookingReference(request.getBookingReference());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setCardLastFour(request.getCardLastFour());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setBookingId(payment.getBookingId());
        response.setBookingReference(payment.getBookingReference());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCardLastFour(payment.getCardLastFour());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setStatus(payment.getStatus());
        return response;
    }
}
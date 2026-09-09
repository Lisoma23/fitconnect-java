package com.formation.payment.exception;

public class PaymentAlreadyRefundedException extends RuntimeException {

    public PaymentAlreadyRefundedException(Long id) {
        super("Payment already refunded with id: " + id);
    }
}
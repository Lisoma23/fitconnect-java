package com.formation.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentAlreadyRefundedException extends RuntimeException {

    public PaymentAlreadyRefundedException(Long id) {
        super("Payment already refunded with id: " + id);
    }
}
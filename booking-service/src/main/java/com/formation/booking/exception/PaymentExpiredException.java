package com.formation.booking.exception;

public class PaymentExpiredException extends RuntimeException {

    public PaymentExpiredException(Long id) {
        super("Le paiement de la reservation " + id + " a expire");
    }
}

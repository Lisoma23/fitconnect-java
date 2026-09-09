package com.formation.booking.exception;

public class BookingAlreadyCompletedException extends RuntimeException {

    public BookingAlreadyCompletedException(Long id) {
        super("La reservation " + id + " est deja terminee");
    }
}

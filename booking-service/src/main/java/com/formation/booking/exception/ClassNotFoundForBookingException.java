package com.formation.booking.exception;

public class ClassNotFoundForBookingException extends RuntimeException {

    public ClassNotFoundForBookingException(Long classId) {
        super("Le cours " + classId + " demande dans la reservation est introuvable");
    }
}

package com.formation.booking.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super("Service indisponible, impossible de traiter la demande : " + message);
    }
}

package com.formation.booking.exception;

public class CancellationNotAllowedException extends RuntimeException {

    public CancellationNotAllowedException(Long id) {
        super("L'annulation de la reservation " + id + " n'est plus autorisee (delai depasse)");
    }
}

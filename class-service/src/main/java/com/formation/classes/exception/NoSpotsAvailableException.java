package com.formation.classes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NoSpotsAvailableException extends RuntimeException {

    public NoSpotsAvailableException(Long id) {
        super("No spots available for fitness class: " + id);
    }
}
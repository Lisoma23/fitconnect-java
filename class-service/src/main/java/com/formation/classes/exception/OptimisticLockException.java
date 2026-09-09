package com.formation.classes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(Long id) {
        super("Fitness class was modified concurrently: " + id);
    }
}
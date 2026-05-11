package com.exception;

import org.springframework.http.HttpStatus;

public class InvalidActionException extends ApiException {

    public InvalidActionException(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}

package com.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ApiException {
    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

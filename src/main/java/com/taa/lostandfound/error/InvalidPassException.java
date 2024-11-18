package com.taa.lostandfound.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidPassException extends RuntimeException {
    public InvalidPassException(String message) {
        super(message);
    }
}

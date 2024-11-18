package com.taa.lostandfound.error;

public class AlreadyExistsException extends RuntimeException {
    @SuppressWarnings("unused")
    public AlreadyExistsException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

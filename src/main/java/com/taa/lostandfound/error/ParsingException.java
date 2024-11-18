package com.taa.lostandfound.error;

public class ParsingException extends RuntimeException {
    @SuppressWarnings("unused")
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

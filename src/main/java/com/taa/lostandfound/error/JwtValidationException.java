package com.taa.lostandfound.error;

public class JwtValidationException extends JwtException {
    @SuppressWarnings("unused")
    public JwtValidationException(String message) {
        super(message);
    }

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

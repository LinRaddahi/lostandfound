package com.taa.lostandfound.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PDFExtractionException extends RuntimeException {
    public PDFExtractionException(String message) {
        super(message);
    }
}

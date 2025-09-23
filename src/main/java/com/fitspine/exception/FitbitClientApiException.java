package com.fitspine.exception;

public class FitbitClientApiException extends RuntimeException {
    public FitbitClientApiException(String message) {
        super(message);
    }

    public FitbitClientApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

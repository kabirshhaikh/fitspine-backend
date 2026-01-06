package com.fitspine.exception;

public class InvalidUserRegistrationException extends RuntimeException {
    public InvalidUserRegistrationException(String message) {
        super(message);
    }
}

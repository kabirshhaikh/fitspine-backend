package com.fitspine.exception;

public class DailyLogAlreadyExistsException extends RuntimeException {
    public DailyLogAlreadyExistsException(String message) {
        super(message);
    }
}

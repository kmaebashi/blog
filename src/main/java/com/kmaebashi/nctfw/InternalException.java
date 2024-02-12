package com.kmaebashi.nctfw;

public class InternalException extends RuntimeException {
    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Exception cause) {
        super(message, cause);
    }
}

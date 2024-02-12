package com.kmaebashi.jsonparser;

public class JsonParseException extends Exception {
    private final int lineNumber;
    public JsonParseException(String message, int lineNumber) {
        super(message + " at " + lineNumber);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }
}

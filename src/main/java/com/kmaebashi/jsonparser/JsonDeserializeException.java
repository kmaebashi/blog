package com.kmaebashi.jsonparser;

public class JsonDeserializeException extends Exception {
    private final int lineNumber;

    public JsonDeserializeException(String message, int lineNumber) {
        super(message  + "(line:" + lineNumber + ")。");
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }
}

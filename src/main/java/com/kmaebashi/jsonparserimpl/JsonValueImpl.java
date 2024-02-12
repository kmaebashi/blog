package com.kmaebashi.jsonparserimpl;
import com.kmaebashi.jsonparser.JsonValue;
import com.kmaebashi.jsonparser.JsonValueType;

public class JsonValueImpl implements JsonValue {
    private final JsonValueType type;
    private int intValue;
    private double realValue;
    private String stringValue;
    private boolean booleanValue;
    private final int lineNumber;

    public JsonValueImpl(int intValue, int lineNumber) {
        this.type = JsonValueType.INT;
        this.intValue = intValue;
        this.lineNumber = lineNumber;
    }

    public JsonValueImpl(double realValue, int lineNumber) {
        this.type = JsonValueType.REAL;
        this.realValue = realValue;
        this.lineNumber = lineNumber;
    }

    public JsonValueImpl(String stringValue, int lineNumber) {
        this.type = JsonValueType.STRING;
        this.stringValue = stringValue;
        this.lineNumber = lineNumber;
    }

    public JsonValueImpl(boolean booleanValue, int lineNumber) {
        this.type = JsonValueType.BOOLEAN;
        this.booleanValue = booleanValue;
        this.lineNumber = lineNumber;
    }

    public JsonValueImpl(int lineNumber) {
        this.type = JsonValueType.NULL;
        this.lineNumber = lineNumber;
    }

    @Override
    public JsonValueType getType() {
        return this.type;
    }

    @Override
    public int getInt() {
        return this.intValue;
    }

    @Override
    public double getReal() {
        return this.realValue;
    }

    @Override
    public String getString() {
        return this.stringValue;
    }

    @Override
    public boolean getBoolean() {
        return this.booleanValue;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public String stringify() {
        StringBuilder sb = new StringBuilder();
        stringifySub(sb);
        return sb.toString();
    }

    void stringifySub(StringBuilder sb) {
        switch (this.type) {
            case INT:
                sb.append("" + this.intValue);
                break;
            case REAL:
                sb.append("" + this.realValue);
                break;
            case STRING:
                sb.append("\"" + escapeJsonString(this.stringValue) + "\"");
                break;
            case BOOLEAN:
                sb.append(this.booleanValue ? "true" : "false");
                break;
            case NULL:
                sb.append("null");
                break;
            default:
                assert(false);
        }
    }

    private String escapeJsonString(String src) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < src.length(); i++) {
            if (src.charAt(i) == '"') {
                sb.append("\\\"");
            } else if (src.charAt(i) == '\\') {
                sb.append("\\\\");
            } else if (src.charAt(i) == '\b') {
                sb.append("\\b");
            } else if (src.charAt(i) == '\f') {
                sb.append("\\f");
            } else if (src.charAt(i) == '\n') {
                sb.append("\\n");
            } else if (src.charAt(i) == '\r') {
                sb.append("\\r");
            } else if (src.charAt(i) == '\t') {
                sb.append("\\t");
            } else {
                sb.append(src.charAt(i));
            }
        }

        return sb.toString();
    }
}

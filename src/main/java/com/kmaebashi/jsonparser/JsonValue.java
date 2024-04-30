package com.kmaebashi.jsonparser;

import com.kmaebashi.jsonparserimpl.JsonValueImpl;

public interface JsonValue extends JsonElement {
    JsonValueType getType();

    int getInt();

    double getReal();

    String getString();

    boolean getBoolean();

    @Override
    String stringify();

    static JsonValue createIntValue(int intValue) {
        return new JsonValueImpl(intValue, 0);
    }

    static JsonValue createRealValue(double doubleValue) {
        return new JsonValueImpl(doubleValue, 0);
    }

    static JsonValue createStringValue(String stringValue) {
        return new JsonValueImpl(stringValue, 0);
    }

    static JsonValue createBooleanValue(boolean booleanValue) {
        return new JsonValueImpl(booleanValue, 0);
    }

    static JsonValue createNullValue() {
        return new JsonValueImpl(0);
    }
}
